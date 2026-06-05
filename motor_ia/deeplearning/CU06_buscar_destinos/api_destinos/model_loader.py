import os
import pickle

import tensorflow as tf

CU06_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_PATH = os.path.join(CU06_DIR, 'model_files', 'modelo_destinos_final.keras')
CLASSES_PATH = os.path.join(CU06_DIR, 'model_files', 'class_names_final.pkl')


class DestinoClassifier:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._load_model()
        return cls._instance

    def _load_model(self):
        print("Cargando modelo de clasificación de destinos...")
        # Parche para compatibilidad: el modelo fue entrenado con una versión de
        # Keras que serializa 'quantization_config' en las capas Dense, pero la
        # versión local no la reconoce. Se elimina antes de deserializar.
        _original_dense_init = tf.keras.layers.Dense.__init__

        def _patched_dense_init(self_layer, *args, **kwargs):
            kwargs.pop('quantization_config', None)
            _original_dense_init(self_layer, *args, **kwargs)

        tf.keras.layers.Dense.__init__ = _patched_dense_init
        try:
            self.model = tf.keras.models.load_model(MODEL_PATH)
        finally:
            tf.keras.layers.Dense.__init__ = _original_dense_init

        with open(CLASSES_PATH, 'rb') as f:
            self.class_names = pickle.load(f)
        print(f"Modelo cargado. Clases: {self.class_names}")

    def predict(self, image_array):
        """Recibe un array preprocesado (batch, 224,224,3) y devuelve (clase, confianza)."""
        preds = self.model.predict(image_array, verbose=0)
        idx = preds[0].argmax()
        confidence = preds[0][idx]
        return self.class_names[idx], float(confidence)


clasificador = DestinoClassifier()
