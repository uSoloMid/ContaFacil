package com.contafacil.contafacil;

import com.contafacil.contafacil.model.Factura;
import com.contafacil.contafacil.repository.FacturaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.time.LocalDateTime;

import java.util.List; // <-- AÑADIDO para mostrar lista de facturas

@Controller
public class XmlController {

    @Autowired
    private FacturaRepository facturaRepository;

    @GetMapping("/")
    public String index() {
        return "upload";  // plantilla para subir archivo
    }

    @PostMapping("/upload")
    public String uploadXml(@RequestParam("file") MultipartFile file, Model model) {
        try {
            InputStream is = file.getInputStream();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            Element comprobante = doc.getDocumentElement();

            Element emisor = (Element) doc.getElementsByTagNameNS("http://www.sat.gob.mx/cfd/4", "Emisor").item(0);
            Element receptor = (Element) doc.getElementsByTagNameNS("http://www.sat.gob.mx/cfd/4", "Receptor").item(0);
            Element timbre = (Element) doc.getElementsByTagNameNS("http://www.sat.gob.mx/TimbreFiscalDigital", "TimbreFiscalDigital").item(0);

            // Extraer atributos del comprobante
            String serie = comprobante.getAttribute("Serie");
            String folio = comprobante.getAttribute("Folio");
            String fechaStr = comprobante.getAttribute("Fecha");
            String formaPago = comprobante.getAttribute("FormaPago");
            String moneda = comprobante.getAttribute("Moneda");
            String subtotalStr = comprobante.getAttribute("SubTotal");
            String totalStr = comprobante.getAttribute("Total");
            String tipoComprobante = comprobante.getAttribute("TipoDeComprobante");
            String metodoPago = comprobante.getAttribute("MetodoPago");

            LocalDateTime fecha = null;
            if (fechaStr != null && !fechaStr.isEmpty()) {
                fecha = LocalDateTime.parse(fechaStr);
            }

            Double subtotal = subtotalStr.isEmpty() ? 0.0 : Double.parseDouble(subtotalStr);
            Double total = totalStr.isEmpty() ? 0.0 : Double.parseDouble(totalStr);

            String emisorNombre = emisor != null ? emisor.getAttribute("Nombre") : "No encontrado";
            String receptorNombre = receptor != null ? receptor.getAttribute("Nombre") : "No encontrado";
            String uuid = timbre != null ? timbre.getAttribute("UUID") : "No encontrado";

            Factura factura = new Factura();
            factura.setSerie(serie);
            factura.setFolio(folio);
            factura.setFecha(fecha);
            factura.setFormaPago(formaPago);
            factura.setMoneda(moneda);
            factura.setSubtotal(subtotal);
            factura.setTotal(total);
            factura.setTipoComprobante(tipoComprobante);
            factura.setMetodoPago(metodoPago);
            factura.setEmisorNombre(emisorNombre);
            factura.setReceptorNombre(receptorNombre);
            factura.setUuid(uuid);

            facturaRepository.save(factura);

            model.addAttribute("message", "Archivo cargado y guardado exitosamente.");
            model.addAttribute("emisorNombre", emisorNombre);
            model.addAttribute("receptorNombre", receptorNombre);
            model.addAttribute("uuid", uuid);

        } catch (Exception e) {
            model.addAttribute("message", "Error al procesar el XML: " + e.getMessage());
            model.addAttribute("emisorNombre", "");
            model.addAttribute("receptorNombre", "");
            model.addAttribute("uuid", "");
        }

        return "upload-result";
    }

    // 👉 NUEVO MÉTODO PARA MOSTRAR FACTURAS EN UNA TABLA
    @GetMapping("/facturas")
    public String mostrarFacturas(Model model) {
        List<Factura> listaFacturas = facturaRepository.findAll();
        model.addAttribute("facturas", listaFacturas);
        return "facturas";
    }
}
