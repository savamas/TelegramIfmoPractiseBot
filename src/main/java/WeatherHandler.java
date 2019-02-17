import DTO.Weather;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class WeatherHandler {
    public  static Weather getWeather(String message, Weather weather) throws IOException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + message +"&units=metric&appid=9f4f7122f3dc1c1c8d5ec3e9746aac8e");
        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while(in.hasNext()){
            result += in.nextLine();
        }

        JSONObject object = new JSONObject(result);
        weather.setName(object.getString("name"));

        JSONObject main = object.getJSONObject("main");
        weather.setTemp(main.getDouble("temp"));
        weather.setHumidity(main.getDouble("humidity"));
        JSONArray weatherArray = object.getJSONArray("weather");
        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject obj = weatherArray.getJSONObject(i);
            weather.setIcon((String) obj.get("icon"));
            weather.setMain((String) obj.get("main"));
        }
        return weather;
//
//        return "City: " + weather.getName() + "\n" +
//                "Temperature: " + weather.getTemp() + "°С\n" +
//                "Humidity: " + weather.getHumidity() + "%\n" +
//                "Main: " + weather.getMain() + "\n" +
//                "https://openweathermap.org/img/w/" + weather.getIcon() + ".png";
    }
}
