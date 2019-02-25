package com.example.komal.myapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    private HashMap<String,String> getSingleNearbyPlace(JSONObject googlePlaceJSON){
        HashMap<String,String> googlePlaceMap=new HashMap<>();
        String NameOfPlace="-NA-";
        String vicinity="-NA-";
        String lat="";
        String lon="";
        String reference="";


        try {
            if (!googlePlaceJSON.isNull("name")){
                NameOfPlace = googlePlaceJSON.getString("name");}
            if (!googlePlaceJSON.isNull("vicinity")){
               vicinity= googlePlaceJSON.getString("vicinity");}
            lat= ( String ) googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").get("lat");
            lon= ( String ) googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").get("lng");
            reference = googlePlaceJSON.getString("reference");
            googlePlaceMap.put("place_name",NameOfPlace);
            googlePlaceMap.put("vicinity",vicinity);
            googlePlaceMap.put("lat",lat);
            googlePlaceMap.put("lng",lon);
            googlePlaceMap.put("reference",reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;

    }
    private List<HashMap<String,String>> getAllNearbyPlaces(JSONArray jsonArray){
        int count=jsonArray.length();

        List<HashMap<String,String>> nearbyPlacesList=new ArrayList<>();
        HashMap<String,String> nearbyPlaceMap=null;

        for(int i=0;i<count;i++){
            try {
                nearbyPlaceMap=getSingleNearbyPlace((JSONObject)jsonArray.get(i));
                nearbyPlacesList.add(nearbyPlaceMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return nearbyPlacesList;
    }

    public List<HashMap<String,String>> parse(String jsonData){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;


        try {

            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getAllNearbyPlaces(jsonArray);

    }

}
