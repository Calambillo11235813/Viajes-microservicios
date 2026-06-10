package pdf

import (
	"bytes"
	"fmt"

	"github.com/jung-kurt/gofpdf"
	"github.com/skip2/go-qrcode"
)

const (
	colorPrimaryR = 41
	colorPrimaryG = 98
	colorPrimaryB = 255
	colorCardR    = 248
	colorCardG    = 250
	colorCardB    = 252
	colorBorderR  = 226
	colorBorderG  = 232
	colorBorderB  = 240
	colorTextR    = 30
	colorTextG    = 41
	colorTextB    = 59
	colorMutedR   = 100
	colorMutedG   = 116
	colorMutedB   = 139
)

// GenerateTicketPDF genera un comprobante de viaje en PDF a partir de los datos del payload.
func GenerateTicketPDF(data map[string]interface{}) ([]byte, error) {
	nombre := stringField(data, "nombre")
	origen := stringField(data, "origen")
	destino := stringField(data, "destino")
	fecha := stringField(data, "fecha")
	hora := stringField(data, "hora")
	asiento := stringField(data, "asiento")
	email := stringField(data, "email")
	referencia := stringField(data, "id_reserva")
	if referencia == "-" {
		referencia = stringField(data, "referencia")
	}
	if referencia == "-" {
		referencia = fmt.Sprintf("%s-%s", fecha, asiento)
	}

	doc := gofpdf.New("P", "mm", "A4", "")
	tr := doc.UnicodeTranslatorFromDescriptor("cp1252")
	doc.AddPage()

	drawTicketCard(doc, tr, ticketData{
		nombre:     nombre,
		origen:     origen,
		destino:    destino,
		fecha:      fecha,
		hora:       hora,
		asiento:    asiento,
		email:      email,
		referencia: referencia,
	})

	var buf bytes.Buffer
	if err := doc.Output(&buf); err != nil {
		return nil, fmt.Errorf("generar PDF: %w", err)
	}

	return buf.Bytes(), nil
}

type ticketData struct {
	nombre, origen, destino, fecha, hora, asiento, email, referencia string
}

func drawTicketCard(doc *gofpdf.Fpdf, tr func(string) string, t ticketData) {
	const (
		cardX = 15.0
		cardY = 18.0
		cardW = 180.0
		cardH = 175.0
	)

	// Sombra suave
	doc.SetFillColor(220, 225, 235)
	doc.RoundedRect(cardX+1.5, cardY+1.5, cardW, cardH, 4, "1234", "F")

	// Tarjeta principal
	doc.SetFillColor(colorCardR, colorCardG, colorCardB)
	doc.SetDrawColor(colorBorderR, colorBorderG, colorBorderB)
	doc.SetLineWidth(0.4)
	doc.RoundedRect(cardX, cardY, cardW, cardH, 4, "1234", "FD")

	// Cabecera
	doc.SetFillColor(colorPrimaryR, colorPrimaryG, colorPrimaryB)
	doc.RoundedRect(cardX, cardY, cardW, 28, 4, "1234", "F")
	doc.Rect(cardX, cardY+20, cardW, 8, "F")

	doc.SetTextColor(255, 255, 255)
	doc.SetFont("Arial", "B", 18)
	doc.SetXY(cardX+12, cardY+8)
	doc.Cell(0, 8, tr("Comprobante de Viaje"))

	doc.SetFont("Arial", "", 9)
	doc.SetXY(cardX+12, cardY+17)
	doc.Cell(0, 5, tr("Agencia de Viajes · Boleto electrónico"))

	// Referencia
	doc.SetTextColor(200, 220, 255)
	doc.SetFont("Arial", "B", 8)
	doc.SetXY(cardX+cardW-58, cardY+10)
	doc.Cell(46, 4, tr("REF."))
	doc.SetFont("Arial", "", 9)
	doc.SetXY(cardX+cardW-58, cardY+15)
	doc.Cell(46, 5, tr(t.referencia))

	// Pasajero
	contentY := cardY + 36
	doc.SetTextColor(colorMutedR, colorMutedG, colorMutedB)
	doc.SetFont("Arial", "", 9)
	doc.SetXY(cardX+12, contentY)
	doc.Cell(0, 5, tr("PASAJERO"))

	doc.SetTextColor(colorTextR, colorTextG, colorTextB)
	doc.SetFont("Arial", "B", 16)
	doc.SetXY(cardX+12, contentY+7)
	doc.Cell(0, 8, tr(t.nombre))

	// Ruta origen → destino
	routeY := contentY + 24
	doc.SetFillColor(255, 255, 255)
	doc.SetDrawColor(colorBorderR, colorBorderG, colorBorderB)
	doc.RoundedRect(cardX+10, routeY, cardW-20, 28, 3, "1234", "FD")

	doc.SetTextColor(colorMutedR, colorMutedG, colorMutedB)
	doc.SetFont("Arial", "", 8)
	doc.SetXY(cardX+16, routeY+4)
	doc.Cell(40, 4, tr("ORIGEN"))
	doc.SetXY(cardX+cardW-56, routeY+4)
	doc.Cell(40, 4, tr("DESTINO"))

	doc.SetTextColor(colorTextR, colorTextG, colorTextB)
	doc.SetFont("Arial", "B", 13)
	doc.SetXY(cardX+16, routeY+12)
	doc.Cell(70, 7, tr(t.origen))

	doc.SetTextColor(colorPrimaryR, colorPrimaryG, colorPrimaryB)
	doc.SetFont("Arial", "B", 11)
	doc.SetXY(cardX+88, routeY+13)
	doc.Cell(14, 6, tr("→"))

	doc.SetTextColor(colorTextR, colorTextG, colorTextB)
	doc.SetFont("Arial", "B", 13)
	doc.SetXY(cardX+cardW-76, routeY+12)
	doc.Cell(70, 7, tr(t.destino))

	// Detalles en cuadrícula
	gridY := routeY + 36
	details := []struct{ label, value string }{
		{"Fecha de viaje", t.fecha},
		{"Hora de salida", t.hora},
		{"Asiento", t.asiento},
		{"Correo", t.email},
	}

	colW := (cardW - 24) / 2
	for i, item := range details {
		col := i % 2
		row := i / 2
		x := cardX + 12 + float64(col)*colW
		y := gridY + float64(row)*22

		doc.SetFillColor(255, 255, 255)
		doc.SetDrawColor(colorBorderR, colorBorderG, colorBorderB)
		doc.RoundedRect(x, y, colW-4, 18, 2, "1234", "FD")

		doc.SetTextColor(colorMutedR, colorMutedG, colorMutedB)
		doc.SetFont("Arial", "", 8)
		doc.SetXY(x+4, y+3)
		doc.Cell(colW-8, 4, tr(item.label))

		doc.SetTextColor(colorTextR, colorTextG, colorTextB)
		doc.SetFont("Arial", "B", 11)
		doc.SetXY(x+4, y+9)
		doc.Cell(colW-8, 6, tr(item.value))
	}

	// QR
	qrY := gridY + 50
	qrContent := fmt.Sprintf("BOLETO|%s|%s|%s|%s|%s", t.referencia, t.nombre, t.origen, t.destino, t.fecha)
	if err := drawQRCode(doc, qrContent, cardX+12, qrY, 32); err != nil {
		doc.SetTextColor(colorMutedR, colorMutedG, colorMutedB)
		doc.SetFont("Arial", "I", 9)
		doc.SetXY(cardX+12, qrY+10)
		doc.Cell(80, 5, tr("QR no disponible"))
	}

	doc.SetTextColor(colorMutedR, colorMutedG, colorMutedB)
	doc.SetFont("Arial", "", 8)
	doc.SetXY(cardX+48, qrY+4)
	doc.Cell(90, 4, tr("Escanea para verificar tu boleto"))
	doc.SetFont("Arial", "B", 9)
	doc.SetXY(cardX+48, qrY+10)
	doc.Cell(90, 5, tr(t.referencia))

	doc.SetFont("Arial", "", 8)
	doc.SetXY(cardX+48, qrY+18)
	doc.MultiCell(95, 4, tr("Presenta este comprobante al abordar. Válido únicamente para la fecha y ruta indicadas."), "", "L", false)

	// Pie de página
	doc.SetDrawColor(colorBorderR, colorBorderG, colorBorderB)
	doc.Line(cardX+10, cardY+cardH-14, cardX+cardW-10, cardY+cardH-14)

	doc.SetTextColor(colorMutedR, colorMutedG, colorMutedB)
	doc.SetFont("Arial", "I", 8)
	doc.SetXY(cardX+12, cardY+cardH-10)
	doc.Cell(0, 4, tr("Documento generado automáticamente. Conserve este comprobante para su viaje."))
}

func drawQRCode(doc *gofpdf.Fpdf, content string, x, y, size float64) error {
	png, err := qrcode.Encode(content, qrcode.Medium, 256)
	if err != nil {
		return err
	}

	opt := gofpdf.ImageOptions{ImageType: "PNG", ReadDpi: true}
	reader := bytes.NewReader(png)
	doc.RegisterImageOptionsReader("ticket-qr", opt, reader)
	doc.ImageOptions("ticket-qr", x, y, size, size, false, opt, 0, "")
	return nil
}

func stringField(data map[string]interface{}, key string) string {
	value, ok := data[key]
	if !ok || value == nil {
		return "-"
	}

	switch v := value.(type) {
	case string:
		if v == "" {
			return "-"
		}
		return v
	case float64:
		return fmt.Sprintf("%.0f", v)
	case int:
		return fmt.Sprintf("%d", v)
	case int64:
		return fmt.Sprintf("%d", v)
	default:
		return fmt.Sprintf("%v", v)
	}
}
