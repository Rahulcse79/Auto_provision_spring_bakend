package com.coraltele.autoprovisioning.component.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.coraltele.autoprovisioning.component.entity.DeviceManagerCiscoHistory;
import com.coraltele.autoprovisioning.component.entity.PostDataOfCisco;
import com.coraltele.autoprovisioning.component.helper.Constants;
import com.coraltele.autoprovisioning.component.helper.RequestResponse;
import com.coraltele.autoprovisioning.component.repository.DeviceManagerCiscoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeviceManagerCiscoService {

    @Autowired
    private DeviceManagerCiscoRepository repository;

    @Autowired
    private DeviceManagerService service;

    public DeviceManagerCiscoHistory save(DeviceManagerCiscoHistory info) {
        return repository.save(info);
    }

    public Iterable<DeviceManagerCiscoHistory> getall() {
        return repository.findAll();
    }

    public DeviceManagerCiscoHistory getDataById(int id) {
        return repository.findById(id).orElse(null);
    }

    public DeviceManagerCiscoHistory findByMacAddress(String macAddress) {
        Iterable<DeviceManagerCiscoHistory> response = repository.findAll();
        for (DeviceManagerCiscoHistory history : response) {
            if (history.getMacAddress().equals(macAddress)) {
                return history;
            }
        }
        return null;
    }

    public DeviceManagerCiscoHistory findData(String token, String macAddress ) {
        return repository.findByMacAddress(macAddress);
    }

    public int delete(int id) {
        repository.deleteById(id);
        return id;
    }

    public boolean checkFilePresence(String filePath) {
        Path path = Paths.get(filePath);
        try {
            if (Files.exists(path)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void createFileIfNotExists(String fileName) {
        String filePath = "/srv/tftp/" + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        String initialContent = "Initial content";
                        fos.write(initialContent.getBytes());
                    }
                }
            } catch (IOException e) {
                System.err.println("An error occurred while creating the file.");
            }
        }
    }

    public void copyFile(String fileName) {
        String sourceFileName = "/srv/tftp/Sample.cnf.xml";
        String destinationFileName = "/srv/tftp/" + fileName;
        File sourceFile = new File(sourceFileName);
        File destinationFile = new File(destinationFileName);

        try (FileInputStream fis = new FileInputStream(sourceFile);
                FileOutputStream fos = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("An error occurred while copying the file.");
        }
    }

    // Method to read the file content.
    private byte[] readFileContent(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public boolean UpdateCiscoFileAndProvisioning(String fileName, byte[] fileData, String extension,
            PostDataOfCisco postData) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(fileData));
            NodeList ntpList = document.getElementsByTagName("ntp");
            for (int i = 0; i < ntpList.getLength(); i++) {
                Node ntpNode = ntpList.item(i);
                NodeList childNodes = ntpNode.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node childNode = childNodes.item(j);
                    if (childNode.getNodeName().equals("name")) {
                        childNode.setTextContent(postData.getSipServer());
                    }
                }
            }
            NodeList lineList = document.getElementsByTagName("line");
            for (int i = 0; i < lineList.getLength(); i++) {
                Node lineNode = lineList.item(i);
                if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element lineElement = (Element) lineNode;
                    if ("1".equals(lineElement.getAttribute("button"))) {
                        NodeList childNodes = lineElement.getChildNodes();
                        for (int j = 0; j < childNodes.getLength(); j++) {
                            Node childNode = childNodes.item(j);
                            if (childNode.getNodeName().equals("name")) {
                                childNode.setTextContent(extension);
                            }
                        }
                    }
                }
            }
            NodeList contact = document.getElementsByTagName("contact");
            if (contact.getLength() > 0) {
                for (int i = 0; i < contact.getLength(); i++) {
                    Node sipcontact = contact.item(i);
                    sipcontact.setTextContent(extension);
                }
            }
            NodeList phoneLabel = document.getElementsByTagName("phoneLabel");
            if (phoneLabel.getLength() > 0) {
                for (int i = 0; i < phoneLabel.getLength(); i++) {
                    Node sipphoneLabel = phoneLabel.item(i);
                    sipphoneLabel.setTextContent(extension);
                }
            }
            NodeList authName = document.getElementsByTagName("authName");
            if (authName.getLength() > 0) {
                for (int i = 0; i < authName.getLength(); i++) {
                    Node sipauthName = authName.item(i);
                    sipauthName.setTextContent(extension);
                }
            }
            NodeList sipPort = document.getElementsByTagName("sipPort");
            if (sipPort.getLength() > 0) {
                for (int i = 0; i < sipPort.getLength(); i++) {
                    Node setsipPort = sipPort.item(i);
                    setsipPort.setTextContent(postData.getPort());
                }
            }
            NodeList sipPortNodes = document.getElementsByTagName("securedSipPort");
            if (sipPortNodes.getLength() > 0) {
                for (int i = 0; i < sipPortNodes.getLength(); i++) {
                    Node sipPortNode = sipPortNodes.item(i);
                    sipPortNode.setTextContent(postData.getSecurePort());
                }
            }
            NodeList sipServerNodes = document.getElementsByTagName("processNodeName");
            if (sipServerNodes.getLength() > 0) {
                for (int i = 0; i < sipServerNodes.getLength(); i++) {
                    Node sipServerNode = sipServerNodes.item(i);
                    sipServerNode.setTextContent(postData.getSipServer());
                }
            }
            NodeList displayName = document.getElementsByTagName("displayName");
            if (displayName.getLength() > 0) {
                for (int i = 0; i < displayName.getLength(); i++) {
                    Node sipDisplayName = displayName.item(i);
                    sipDisplayName.setTextContent(extension);
                }
            }
            NodeList authPassword = document.getElementsByTagName("authPassword");
            if (authPassword.getLength() > 0) {
                for (int i = 0; i < authPassword.getLength(); i++) {
                    Node sipAuthPassword = authPassword.item(i);
                    sipAuthPassword.setTextContent(postData.getAuthenticateID());
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);
            byte[] updatedFileData = outputStream.toByteArray();
            String outputPath = "/srv/tftp/" + fileName;
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(updatedFileData);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Cisco history
    public boolean ciscoHistory(String macAddress, String token) {
        try {
            String url = "http://" + Constants.DEVICE_MANAGER_IP + ":" + Constants.Node_server_Port + "/diagnosis";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            String requestBody = "{ \"macAddress\": \"" + macAddress + "\" }";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    String responseBody = new String(response.getBody());
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(responseBody);
                    boolean Status = rootNode.path("status").asInt() == 0;
                    if (Status) {
                        JsonNode dataNode = rootNode.path("data");
                        boolean Dhcp = dataNode.path("Dhcp").asInt() == 1;
                        boolean Tfcp = dataNode.path("Tfcp").asInt() == 1;
                        boolean Path = dataNode.path("Path").asInt() == 1;
                        boolean DefaultFile = dataNode.path("DefaultFile").asInt() == 1;
                        String[] dateTimeIST = service.getCurrentDateTimeIST();
                        String date = dateTimeIST[0];
                        String time = dateTimeIST[1];
                        DeviceManagerCiscoHistory device = new DeviceManagerCiscoHistory();
                        device.setDate(date);
                        device.setTime(time);
                        device.setDhcp(Dhcp);
                        device.setTftp(Tfcp);
                        device.setFilePresent(Path);
                        device.setMacAddress(macAddress);
                        device.setDefaultFile(DefaultFile);
                        DeviceManagerCiscoHistory responce = findByMacAddress(macAddress);
                        if(responce != null){
                            repository.deleteById(responce.getId());
                        }
                        repository.save(device);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cisco sp - 3905 function.
    public RequestResponse CiscoConfig(String token, String macAddress, PostDataOfCisco postData) {
        RequestResponse result = new RequestResponse();
        try {
            List<Integer> answer = new ArrayList<>();
            String fileName = "SEP" + postData.getMacAddress() + ".cnf.xml";
            String filePath = Constants.Tftp_dir + fileName;
            boolean fileExist = checkFilePresence(filePath);
            String extension;
            byte[] fileData;
            boolean response = false;
            if (checkFilePresence(Constants.Tftp_dir + Constants.Tftp_sample_file_name)) {
                if (!fileExist) {
                    createFileIfNotExists(fileName);
                    copyFile(fileName);
                }
                fileExist = checkFilePresence(filePath);
                if (fileExist) {
                    extension = postData.getExtension();
                    fileData = readFileContent(filePath);
                    response = UpdateCiscoFileAndProvisioning(fileName, fileData, extension, postData);
                    boolean saveHistory = ciscoHistory(macAddress, token);
                }
                answer.add(response ? 1 : -1);
                for (int i = 0; i < postData.getMacAddressBulk().size(); i++) {
                    fileName = "SEP" + postData.getMacAddressBulk().get(i) + ".cnf.xml";
                    filePath = Constants.Tftp_dir + fileName;
                    response = false;
                    fileExist = checkFilePresence(filePath);
                    if (!fileExist) {
                        createFileIfNotExists(fileName);
                        copyFile(fileName);
                    }
                    fileExist = checkFilePresence(filePath);
                    if (fileExist) {
                        fileData = readFileContent(filePath);
                        extension = postData.getMacExtensionBulk().get(i);
                        response = UpdateCiscoFileAndProvisioning(fileName, fileData, extension, postData);
                        boolean saveHistory = ciscoHistory(macAddress, token);
                    }
                    answer.add(response ? 1 : -1);
                }
                if (answer.size() > 0) {
                    result.setAns(answer);
                    result.setStatus(0);
                    result.setMessage("Cisco configuration successfully.");
                } else {
                    result.setStatus(1);
                    result.setMessage("Failed Cisco configuration.");
                }
            } else {
                result.setStatus(1);
                result.setMessage("Failed Cisco configuration samle file not present.");
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            e.printStackTrace();
            result.setStatus(e.getStatusCode().value());
            result.setMessage("HTTP error: " + e.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }

}
