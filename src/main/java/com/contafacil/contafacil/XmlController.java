package com.contafacil.contafacil;

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

@Controller
public class XmlController {

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

            Element emisor = (Element) doc.getElementsByTagNameNS("http://www.sat.gob.mx/cfd/4", "Emisor").item(0);
            Element receptor = (Element) doc.getElementsByTagNameNS("http://www.sat.gob.mx/cfd/4", "Receptor").item(0);
            Element timbre = (Element) doc.getElementsByTagNameNS("http://www.sat.gob.mx/TimbreFiscalDigital", "TimbreFiscalDigital").item(0);

            String emisorNombre = emisor != null ? emisor.getAttribute("Nombre") : "No encontrado";
            String receptorNombre = receptor != null ? receptor.getAttribute("Nombre") : "No encontrado";
            String uuid = timbre != null ? timbre.getAttribute("UUID") : "No encontrado";

            model.addAttribute("message", "Archivo cargado exitosamente.");
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
}
