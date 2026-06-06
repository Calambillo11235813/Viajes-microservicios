import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  Modal,
  FlatList,
} from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import * as Speech from 'expo-speech';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, TYPOGRAPHY, globalStyles } from '@/theme/theme';
import { CONFIG } from '@/utils/config';
import { styles } from './styles/TraducirVisualScreen.styles';

type EstadoTraduccion = 'enfocando' | 'traduciendo' | 'resultado';

const IDIOMAS = [
  { id: 'es', label: 'Español' },
  { id: 'en', label: 'Inglés' },
  { id: 'it', label: 'Italiano' },
  { id: 'fr', label: 'Francés' },
  { id: 'de', label: 'Alemán' },
];

/**
 * Pantalla de Traducción Visual con IA y Voz (CU-08).
 */
export default function TraducirVisualScreen() {
  const [permiso, pedirPermiso] = useCameraPermissions();
  const [estado, setEstado] = useState<EstadoTraduccion>('enfocando');
  const [textoTraducido, setTextoTraducido] = useState('');
  const [imagenCapturada, setImagenCapturada] = useState<string | null>(null); // FIX: Realidad Aumentada
  const [listaDetecciones, setListaDetecciones] = useState<any[]>([]); // FIX: Realidad Aumentada
  const [medidasFoto, setMedidasFoto] = useState({ width: 1, height: 1 }); // FIX: Matemática AR
  const [layoutAR, setLayoutAR] = useState({ width: 1, height: 1 }); // FIX: Matemática AR
  
  const [idiomaOrigen, setIdiomaOrigen] = useState('en');
  const [idiomaDestino, setIdiomaDestino] = useState('es');
  
  // Modales para selectores
  const [modalVisible, setModalVisible] = useState(false);
  const [tipoSelector, setTipoSelector] = useState<'origen' | 'destino'>('origen');

  const cameraRef = useRef<CameraView>(null);

  useEffect(() => {
    if (permiso && !permiso.granted && permiso.canAskAgain) {
      pedirPermiso();
    }
  }, [permiso, pedirPermiso]);

  if (!permiso) return <View style={globalStyles.safeAreaContainer} />;

  if (!permiso.granted) {
    return (
      <View style={[globalStyles.safeAreaContainer, styles.centerContent]}>
        <Ionicons name="camera-outline" size={64} color={COLORS.placeholder} />
        <Text style={styles.permissionText}>
          Necesitamos acceso a tu cámara para traducir los textos de tu entorno.
        </Text>
        <TouchableOpacity style={globalStyles.btnPrimary} onPress={pedirPermiso}>
          <Text style={TYPOGRAPHY.buttonText}>Conceder Permiso</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const abrirSelector = (tipo: 'origen' | 'destino') => {
    setTipoSelector(tipo);
    setModalVisible(true);
  };

  const seleccionarIdioma = (idIdioma: string) => {
    if (tipoSelector === 'origen') {
      setIdiomaOrigen(idIdioma);
    } else {
      setIdiomaDestino(idIdioma);
    }
    setModalVisible(false);
    if (estado === 'resultado') setEstado('enfocando');
  };

  const alternarIdiomas = () => {
    const temp = idiomaOrigen;
    setIdiomaOrigen(idiomaDestino);
    setIdiomaDestino(temp);
    if (estado === 'resultado') setEstado('enfocando');
  };

  const traducirTexto = async () => {
    if (!cameraRef.current) return;

    setEstado('traduciendo');

    try {
      console.log('Iniciando captura de foto...');
      // 1. Capturar foto
      const foto = await cameraRef.current.takePictureAsync({
        quality: 0.5, // FIX: reducir peso del JPEG para menor latencia de red
        base64: false,
        skipProcessing: true, // Evita que se cuelgue en la segunda captura en Android
      });

      console.log('Foto capturada:', foto?.uri);
      if (!foto || !foto.uri) throw new Error("No se pudo capturar la imagen.");

      setMedidasFoto({ width: foto.width, height: foto.height }); // FIX: Matemática AR
      setImagenCapturada(foto.uri); // FIX: Realidad Aumentada

      // 2. Preparar FormData
      const formData = new FormData();
      formData.append('imagen', {
        uri: foto.uri,
        name: 'captura.jpg',
        type: 'image/jpeg',
      } as any);
      formData.append('idioma_origen', idiomaOrigen);
      formData.append('idioma_destino', idiomaDestino);

      console.log('Enviando fetch a Django...');
      // 3. Enviar a Django
      const response = await fetch(CONFIG.AI_TRANSLATE_URL, {
        method: 'POST',
        body: formData,
        headers: {
          'Accept': 'application/json', // FIX: no setear Content-Type; RN infiere boundary desde FormData
        },
      });

      console.log('Respuesta recibida. Status:', response.status);
      const json = await response.json();

      if (!response.ok || !json.exito) {
        throw new Error(json.error || json.mensaje || 'Error desconocido al traducir.');
      }

      if (!json.detecciones || json.detecciones.length === 0) {
        setListaDetecciones([]); // FIX: Realidad Aumentada
        setTextoTraducido('No se detectó ningún texto en la imagen.');
        setEstado('resultado');
        return;
      }

      // 4. Procesar y leer resultado
      console.log('Traducciones recibidas:', json.detecciones.length);
      setListaDetecciones(json.detecciones); // FIX: Realidad Aumentada
      const textos = json.detecciones.map((d: any) => d.traduccion).join('\n');
      setTextoTraducido(textos);
      setEstado('resultado');

      Speech.speak(textos, { language: idiomaDestino });

    } catch (error: any) {
      console.error('Error en traducción visual:', error);
      Alert.alert(
        'Error',
        error.message || 'Hubo un problema al procesar la traducción. Verifica tu conexión.'
      );
      setEstado('enfocando');
    }
  };

  const leerDeNuevo = () => {
    Speech.speak(textoTraducido, { language: idiomaDestino });
  };

  const detenerAudio = () => {
    Speech.stop(); // FIX: Control de audio
  };

  const reintentar = () => {
    setEstado('enfocando');
    setTextoTraducido('');
    setImagenCapturada(null); // FIX: Realidad Aumentada
    setListaDetecciones([]); // FIX: Realidad Aumentada
    Speech.stop();
  };

  const getLabelIdioma = (id: string) => IDIOMAS.find(i => i.id === id)?.label || id;

  return (
    <View style={styles.container}>
      {/* ─── CÁMARA DE FONDO ─── */}
      <CameraView style={styles.camera} facing="back" ref={cameraRef}>
        
        {/* ─── OVERLAY SUPERIOR (Selector de Idiomas Completo) ─── */}
        <View style={styles.topOverlay}>
          <View style={styles.langSelectorBar}>
            <TouchableOpacity style={styles.langButton} onPress={() => abrirSelector('origen')} activeOpacity={0.8}>
              <Text style={styles.langTextLabel}>Origen</Text>
              <Text style={styles.langTextValue}>{getLabelIdioma(idiomaOrigen)}</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.swapButton} onPress={alternarIdiomas} activeOpacity={0.8}>
              <Ionicons name="swap-horizontal" size={24} color={COLORS.textLight} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.langButton} onPress={() => abrirSelector('destino')} activeOpacity={0.8}>
              <Text style={styles.langTextLabel}>Destino</Text>
              <Text style={styles.langTextValue}>{getLabelIdioma(idiomaDestino)}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* ─── ZONA CENTRAL (Enfoque / Procesando / Resultado) ─── */}
        <View style={styles.centerOverlay}>
          
          {estado === 'enfocando' && (
            <View style={styles.focusFrame}>
              {/* Esquinas del recuadro de enfoque */}
              <View style={[styles.corner, styles.topLeft]} />
              <View style={[styles.corner, styles.topRight]} />
              <View style={[styles.corner, styles.bottomLeft]} />
              <View style={[styles.corner, styles.bottomRight]} />
              <Text style={styles.focusText}>Enfoca el texto y presiona Traducir</Text>
            </View>
          )}

          {estado === 'traduciendo' && (
            <View style={styles.processingCard}>
              <ActivityIndicator size="large" color={COLORS.primary} />
              <Text style={styles.processingText}>Analizando y Traduciendo...</Text>
            </View>
          )}

          {estado === 'resultado' && imagenCapturada && (
            <View
              style={styles.arOverlay}
              onLayout={(event) => setLayoutAR(event.nativeEvent.layout)} // FIX: Matemática AR
            >
              <Image
                source={{ uri: imagenCapturada }}
                style={styles.capturedImage}
                resizeMode="stretch"
              />
              {listaDetecciones.map((d: any, index: number) => {
                const scaleX = layoutAR.width / medidasFoto.width; // FIX: Matemática AR
                const scaleY = layoutAR.height / medidasFoto.height; // FIX: Matemática AR
                const leftScaled = d.coordenadas.top_left[0] * scaleX; // FIX: Matemática AR
                const topScaled = d.coordenadas.top_left[1] * scaleY; // FIX: Matemática AR

                return (
                  <View
                    key={index}
                    style={{
                      position: 'absolute',
                      left: leftScaled,
                      top: topScaled,
                    }}
                  >
                    <Text style={styles.arLabel}>{d.traduccion}</Text>
                  </View>
                );
              })}
              {listaDetecciones.length === 0 && (
                <View style={styles.arNoTextBanner}> {/* FIX: Realidad Aumentada */}
                  <Text style={styles.arLabel}>{textoTraducido}</Text>
                </View>
              )}
            </View>
          )}
        </View>

        {/* ─── OVERLAY INFERIOR (Botones) ─── */}
        <View style={styles.bottomOverlay}>
          {estado === 'enfocando' && (
            <TouchableOpacity style={styles.btnTranslate} onPress={traducirTexto} activeOpacity={0.9}>
              <Ionicons name="scan-outline" size={28} color={COLORS.textLight} />
              <Text style={styles.btnTranslateText}>Traducir</Text>
            </TouchableOpacity>
          )}

          {estado === 'resultado' && (
            <>
              {textoTraducido !== 'No se detectó ningún texto en la imagen.' && (
                <View style={styles.audioControls}>
                  <TouchableOpacity style={styles.btnSpeak} onPress={leerDeNuevo} activeOpacity={0.8}>
                    <Ionicons name="volume-high" size={28} color={COLORS.primary} />
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.btnStopSpeak} onPress={detenerAudio} activeOpacity={0.8}>
                    <Ionicons name="volume-mute" size={28} color={COLORS.danger} />
                  </TouchableOpacity>
                </View>
              )}
              <TouchableOpacity style={styles.btnRetry} onPress={reintentar} activeOpacity={0.8}>
                <Ionicons name="refresh" size={24} color={COLORS.textLight} />
                <Text style={styles.btnRetryText}>Nueva Traducción</Text>
              </TouchableOpacity>
            </>
          )}
        </View>

      </CameraView>

      {/* ─── MODAL DE SELECCIÓN DE IDIOMA ─── */}
      <Modal
        visible={modalVisible}
        transparent={true}
        animationType="slide"
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>
                Seleccionar Idioma de {tipoSelector === 'origen' ? 'Origen' : 'Destino'}
              </Text>
              <TouchableOpacity onPress={() => setModalVisible(false)}>
                <Ionicons name="close" size={24} color={COLORS.textPrimary} />
              </TouchableOpacity>
            </View>
            
            <FlatList
              data={IDIOMAS}
              keyExtractor={(item) => item.id}
              renderItem={({ item }) => {
                const isSelected = (tipoSelector === 'origen' ? idiomaOrigen : idiomaDestino) === item.id;
                return (
                  <TouchableOpacity
                    style={[styles.langOptionItem, isSelected && styles.langOptionSelected]}
                    onPress={() => seleccionarIdioma(item.id)}
                  >
                    <Text style={[styles.langOptionText, isSelected && styles.langOptionTextSelected]}>
                      {item.label}
                    </Text>
                    {isSelected && <Ionicons name="checkmark" size={20} color={COLORS.primary} />}
                  </TouchableOpacity>
                );
              }}
            />
          </View>
        </View>
      </Modal>
    </View>
  );
}
