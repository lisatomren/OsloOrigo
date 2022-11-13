// lisa tomren kjørli
// enkel java-app laget for jobbsøknad i oslo kommune.
// gjør kall mot APIet til oslo bysykler og viser sted, antall ledige sykler og ledige låser.


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import com.google.gson.*;


public class OsloBysykkel {
    
    private String BASEURL = "https://gbfs.urbansharing.com/oslobysykkel.no/";
    
    public static void main(String[] args) {
        
        new OsloBysykkel().RunApp();
    }
    
    
    private void RunApp()
    {
        
            //get all stations
            List<Station>  allStations = GetAllStations();
            
            
            //print info about oslo bicycles
            if( allStations == null )
                System.out.println("Sorry, can't show any bicycles in Oslo");
                
            else
               PrintAllStationsTable(allStations );
        
    }
    
    
    
    private  List<Station> GetAllStations()
    {
        try
        {
            // get all stations with location-name
            String StationListResponse = GetResponseString("station_information.json");
            Response response = new Gson().fromJson(StationListResponse, Response.class );//parse json to object
            List<Station> allStations = response.data.stations;
            

            // get all stations with available locks and bikes
            String stationAvailabilityResponse = GetResponseString("station_status.json");
            Response responseAvailability = new Gson().fromJson(stationAvailabilityResponse, Response.class );//parse json to object
            List<Station> allStationsWithAvailability = responseAvailability.data.stations;
            
            
            // set availability to all stations
            for( Station station : allStations)
            {
                Station matchingStation = allStationsWithAvailability
                .stream()
                .filter(ms -> ms.station_id.equals( station.station_id ))
                .findFirst()
                .orElse(new Station());
                
                station.num_bikes_available = matchingStation.num_bikes_available;
                station.num_docks_available = matchingStation.num_docks_available;
            }
            
            //order by name
            allStations.sort(Comparator.comparing( e -> e.name)); 
            
            return response.data.stations;
            
        }
        catch( Exception ex)
        {
            System.out.println( "Something went wrong in GetAllStaions-method.");
        }
        
        return Collections.<Station>emptyList();
    }
    
    
    private String GetResponseString( String requestUrl )
    {
        
        try
        {
            URL url = new URL(BASEURL + requestUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode = con.getResponseCode();
            
            
            if( responseCode == 200 )
            {
                //get responseString
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                return response.toString();
            }           
        }
        catch( Exception ex)
        {
            System.out.println( "Something went wrong in GetResponseString-method");
        }
        
        return "";
        
    }
    
    
    private void PrintAllStationsTable( List<Station> allStations)
    {
        //heading
        System.out.println("BYSYKLER I OSLO");
        
        //table header
        System.out.format("%-30s %2s %30s %n",
                          "STED:",
                          "LEDIGE SYKLER:",
                          "LEDIGE LÅSER:");
        
        //table rows
        for(Station station : allStations) {
            
            System.out.format("%-30s %2s %32s %n",
                              station.name, 
                              station.num_bikes_available, 
                              station.num_docks_available
                               );
        }
    }
    
    

    //----------OBJECT----------//
    //burde kunne løses med færre objekter..
        
    class Response {
        Data data;
    }

    class Data{
        List<Station> stations;
    }

    class Station
    {
        String station_id;
        String name;
        int num_bikes_available;
        int num_docks_available;
    }
    
    
}
