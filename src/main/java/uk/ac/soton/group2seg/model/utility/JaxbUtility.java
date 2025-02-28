package uk.ac.soton.group2seg.model.utility;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import uk.ac.soton.group2seg.model.Airport;
import uk.ac.soton.group2seg.model.AirportList;

public class JaxbUtility {

  public static Airport loadAirport(String xmlFilePath) {
    try{
      Path xmlPath = Path.of("src/main/resources/" + xmlFilePath);
      JAXBContext context = JAXBContext.newInstance(Airport.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      try(Reader reader = Files.newBufferedReader(xmlPath)) {
        return (Airport) unmarshaller.unmarshal(reader);
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println(e.getMessage());
      }
    }catch (JAXBException e){
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
    return null;
  }

  /**
   * Parses XML File into list of airport names and ids
   * */
  public static AirportList parseAirports() {
    File xmlFile = new File("src/main/Resources/airportList.xml");

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(AirportListXML.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      AirportListXML airportListXML = (AirportListXML) unmarshaller.unmarshal(xmlFile);
      AirportList airportList = new AirportList();

      for(AirportXml airportXml : airportListXML.getAirports()) {
        airportList.addAirportByString(airportXml.getId(), airportXml.getName());
      }

      return airportList;

    }catch (JAXBException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
    return null;
  }

  @XmlRootElement(name = "airportList")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class AirportListXML {

    @XmlElement(name = "airport")
    private List<AirportXml> airports;

    public List<AirportXml> getAirports() {
      return airports;
    }

    public void setAirports(List<AirportXml> airports) {
      this.airports = airports;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  private static class AirportXml {
    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "name")
    private String name;

    private String getId() {
      return id;
    }

    private void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

}
