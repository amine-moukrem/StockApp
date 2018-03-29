/**
 * Author: Amine Moukrem
 * Date last edit: December 9th, 2017
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class StockJSONReader {
    // API Key: VXA9OQNT36ZEX8KZ
    List<Double> vals = new ArrayList<>();
    List<String> dates = new ArrayList<> ();
    String date, dailyOpen, dailyHigh, dailyLow, dailyClose, dailyVolume, dailySma, dailyRsi;
    String weeklyOpen, weeklyHigh, weeklyLow, weeklyClose, weeklyVolume, weeklySma, weeklyRsi;
    DateFormat dateFormat; // Format for date
    DateFormat dateFormatSMA; // Format for SMA date
    DateFormat dateFormatRSI; // Format for RSI date
    Date thisDate;
    DateFormat dateFormatTime;
    Date dateCurrent; // Current date
    Date dateRefreshedDaily; // Time Series (Daily) last date refreshed
    Date dateRefreshedWeekly; // Time Series(Weekly) last date refreshed
    Date dateRefreshedSMADaily; // SMA last date refreshed
    Date dateRefreshedRSIDaily; // RSI last date refreshed
    Date dateRefreshedSMAWeekly;
    Date dateRefreshedRSIWeekly;
    JSONObject stockDailyJson; // Full Time Series (Daily) JSON API call
    JSONObject stockWeeklyJson; // Weekly Time Series JSON API call
    JSONObject smaDailyJson; // Full Simple Moving Average (Daily) JSON call
    JSONObject rsiDailyJson; // Full Relative Strength Index (RSI) daily JSON call
    JSONObject smaWeeklyJson; // Full SMA weekly JSON API call
    JSONObject rsiWeeklyJson; // Full RSI weekly JSON API call
    JSONObject metadataJsonDaily, smaMetaDataJsonDaily, rsiMetaDataJsonDaily; // Daily Meta Data objects for each API call
    JSONObject metadataJsonWeekly, smaMetaDataJsonWeekly, rsiMetaDataJsonWeekly;
    String dailyDateRefreshString, smaDailyDateRefreshString, rsiDailyDateRefreshString, 
            weeklyDateRefreshString, smaWeeklyDateRefreshString, rsiWeeklyDateRefreshString; // These store string values of last refresh in Meta Data objects
    JSONObject timeSeriesObj; // Time Series (Daily) data object
    JSONObject timeSeriesCurrentObj; // Current day Time Series (Daily) data object
    JSONObject timeSeriesWeeklyObj;
    JSONObject timeSeriesCurrentWeeklyObj;
    JSONObject weeklyObj;
    JSONObject weeklyCurrentObj;
    JSONObject smaWeeklyObj;
    JSONObject smaCurrentWeeklyObj;
    JSONObject smaDailyObj; // Simple Moving Average (SMA) data object
    JSONObject smaCurrentDailyObj; // Current day SMA data object
    JSONObject rsiWeeklyObj;
    JSONObject rsiDailyObj; // RSI data object
    JSONObject rsiCurrentDailyObj; // Current day RSI data object
    JSONObject rsiCurrentWeeklyObj;
    JSONArray jsonStockArray; // Array of time series daily data
    JSONArray jsonStockArrayWeekly;
    JSONArray jsonSMAArrayDaily; // Array of daily SMA data
    JSONArray jsonRSIArrayDaily; // Array of daily RSI data
    JSONArray jsonSMAArrayWeekly;
    JSONArray jsonRSIArrayWeekly;
    String currentStock; // Current stock company
    JSONObject todayStock; // Data in current day Time Series
    JSONObject todaySMA; // Data in current day SMA
    JSONObject todayRSI; // Data in current day RSI
    JSONObject todayWeeklyStock;
    JSONObject todayWeeklySMA;
    JSONObject todayWeeklyRSI;
    Calendar cal = Calendar.getInstance();

    public StockJSONReader() throws IOException, JSONException, ParseException {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Sets format for date
        dateFormatSMA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatRSI = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatTime = new SimpleDateFormat("HH:mm");
        dateCurrent = new Date();
        thisDate = new Date();
        dateRefreshedWeekly = new Date();
        dateRefreshedDaily = new Date();
        dateRefreshedSMADaily = new Date();
        dateRefreshedRSIDaily = new Date();
        dateRefreshedSMAWeekly = new Date();
        dateRefreshedRSIWeekly = new Date();
        
        setStockJsonApple(); // Initialize stocks to Apple
        setStockMetaDataJsonDaily(); // Puts meta data from full stockJson object into its own metaDataJson object
        setStockMetaDataJsonWeekly();
        setSMAMetaDataJsonDaily(); // Puts meta data from full smaJson object into its own smaMetaDataJson object
        setSMAMetaDataJsonWeekly();
        setRSIMetaDataJsonDaily(); // Puts meta data for full rsiJson object into its own rsiMetaDataJson object
        setRSIMetaDataJsonWeekly();
        setStockRefsDaily(); // Sets last refreshed and symbol for TimeSeries stock 
        setStockRefsWeekly();
        setSMARefsDaily(); // Sets last refreshed for SMA
        setSMARefsWeekly();
        setRSIRefsDaily(); // Sets last refreshed for RSI
        setRSIRefsWeekly();
        setTimeSeriesDaily(); // Puts Time Series (Daily) data from full stockJson object into its own timeSeriesObj object
        setTimeSeriesWeekly();
        setSMAObjDaily(); // Puts SMA data from full smaJson object into its own smaObj object
        setSMAObjWeekly();
        setRSIObjDaily(); // Puts RSI data from full rsiJson object into its own rsiObj object
        setRSIObjWeekly();
        setDateCurrent(); // Sets and formats today's date
        setDataOpenDaily(todayStock);
        setDataOpenWeekly(todayWeeklyStock);
        setDataHighDaily(todayStock);
        setDataHighWeekly(todayWeeklyStock);
        setDataLowDaily(todayStock);
        setDataLowWeekly(todayWeeklyStock);
        setDataCloseDaily(todayStock);
        setDataCloseWeekly(todayWeeklyStock);
        setDataVolumeDaily(todayStock);
        setDataVolumeWeekly(todayStock);
        setDataSMADaily(todaySMA);
        setDataSMAWeekly(todayWeeklySMA);
        setDataRSIDaily(todayRSI);
        setDataRSIWeekly(todayWeeklyRSI);
    }
    
    public void setStockRefsDaily() {
        dailyDateRefreshString = metadataJsonDaily.getString("3. Last Refreshed"); // Gets date refreshed from meta data object
        currentStock = metadataJsonDaily.getString("2. Symbol"); // Gets current stock name
        try {
        dateRefreshedDaily = dateFormat.parse(dailyDateRefreshString); // Sets date refreshed to the value in meta data object
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    public void setStockRefsWeekly() {
        weeklyDateRefreshString = metadataJsonWeekly.getString("3. Last Refreshed"); // Gets date refreshed from meta data object
        currentStock = metadataJsonWeekly.getString("2. Symbol"); // Gets current stock name
        try {
        dateRefreshedWeekly = dateFormat.parse(weeklyDateRefreshString); // Sets date refreshed to the value in meta data object
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void setSMARefsDaily() throws ParseException {
        smaDailyDateRefreshString = smaMetaDataJsonDaily.getString("3: Last Refreshed"); // Gets date refreshed from meta data object
        try {
            if(dateFormatTime.parse(dateFormatTime.format(thisDate)).after(dateFormatTime.parse("16:00"))){ // Sets date refreshed to the value in meta data object
                dateRefreshedSMADaily = dateFormat.parse(smaDailyDateRefreshString); 
            }
            else {
                dateRefreshedSMADaily = dateFormatSMA.parse(smaDailyDateRefreshString);
            }
        } catch (ParseException e) {
           
        }
    }
    
    public void setSMARefsWeekly() throws ParseException {
        smaWeeklyDateRefreshString = smaMetaDataJsonWeekly.getString("3: Last Refreshed"); // Gets date refreshed from meta data object
        try {
            if(dateFormatTime.parse(dateFormatTime.format(thisDate)).after(dateFormatTime.parse("16:00"))){
                dateRefreshedSMAWeekly = dateFormat.parse(smaWeeklyDateRefreshString); // Sets date refreshed to the value in meta data object
            }
            else {
                 dateRefreshedSMAWeekly = dateFormatSMA.parse(smaWeeklyDateRefreshString); 
            }
        } catch (ParseException e) {
           
        }
    }
    
    public void setRSIRefsDaily() throws ParseException {
        rsiDailyDateRefreshString = rsiMetaDataJsonDaily.getString("3: Last Refreshed"); // Gets date refreshed from meta data object
        try {
            if(dateFormatTime.parse(dateFormatTime.format(thisDate)).after(dateFormatTime.parse("16:00"))){
                dateRefreshedRSIDaily = dateFormat.parse(rsiDailyDateRefreshString); // Sets date refreshed to the value in meta data object
            }
            else {
                dateRefreshedRSIDaily = dateFormatRSI.parse(rsiDailyDateRefreshString);
            }
        } catch (ParseException e) {
           
        }
    }
    
    public void setRSIRefsWeekly() throws ParseException {
        rsiWeeklyDateRefreshString = rsiMetaDataJsonWeekly.getString("3: Last Refreshed"); // Gets date refreshed from meta data object
        try {
            if(dateFormatTime.parse(dateFormatTime.format(thisDate)).after(dateFormatTime.parse("16:00"))){
                dateRefreshedRSIWeekly = dateFormat.parse(rsiWeeklyDateRefreshString); // Sets date refreshed to the value in meta data object
            }
            else {
                dateRefreshedRSIWeekly = dateFormatRSI.parse(rsiWeeklyDateRefreshString);
            }
        } catch (ParseException e) {
            
        }
    }
    
    public void setTimeSeriesDaily() {
        timeSeriesCurrentObj = stockDailyJson.getJSONObject("Time Series (Daily)");

        todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
    }
    
    public void setTimeSeriesWeekly() {
        weeklyCurrentObj = stockWeeklyJson.getJSONObject("Weekly Time Series");
        
        todayWeeklyStock = weeklyObj.getJSONObject(dateFormat.format(dateRefreshedWeekly));
    }
    
    
    public void setSMADaily() throws ParseException {
        int num = 0;
        smaCurrentDailyObj = smaDailyJson.getJSONObject("Technical Analysis: SMA");
        try {
            todaySMA = smaDailyObj.getJSONObject(dateFormatSMA.format(dateRefreshedSMADaily));
        } catch (JSONException e) {
            while (num != 7 && !smaDailyObj.has(dateFormatSMA.format(dateRefreshedSMADaily))) { // Check if there are any values for the past week (7 days)
                dateRefreshedSMADaily = subtractDays(dateRefreshedSMADaily, 1);
                num++;
            }
            todaySMA = smaDailyObj.getJSONObject(dateFormat.format(dateRefreshedSMADaily));
        }
    }
    
    public void setSMAWeekly() {
        smaCurrentWeeklyObj = smaWeeklyJson.getJSONObject("Technical Analysis: SMA");
        
        try {
            todayWeeklySMA = smaWeeklyObj.getJSONObject(dateFormatSMA.format(dateRefreshedSMAWeekly));
        }  catch (JSONException e) {
              todayWeeklySMA = smaWeeklyObj.getJSONObject(dateFormat.format(dateRefreshedSMAWeekly));
        }
    }
    
    public void setRSIDaily() {
        rsiCurrentDailyObj = rsiDailyJson.getJSONObject("Technical Analysis: RSI");
        
        try {
            todayRSI = rsiDailyObj.getJSONObject(dateFormatRSI.format(dateRefreshedRSIDaily));
        } catch (JSONException e) {
              todayRSI = rsiDailyObj.getJSONObject(dateFormat.format(dateRefreshedRSIDaily));
        }
    }
    
    public void setRSIWeekly() {
        rsiCurrentWeeklyObj = rsiWeeklyJson.getJSONObject("Technical Analysis: RSI");
        
        try {
            todayWeeklyRSI = rsiWeeklyObj.getJSONObject(dateFormatRSI.format(dateRefreshedRSIWeekly));
        } catch (JSONException e) {
              todayWeeklyRSI = rsiDailyObj.getJSONObject(dateFormat.format(dateRefreshedRSIDaily));
        }
    }
    
    public static Date addDays(Date date, int days) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            cal.add(Calendar.DATE, days);

            return cal.getTime();
    }
    
    public static Date subtractDays(Date date, int days) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            cal.add(Calendar.DATE, -days);

            return cal.getTime();
    }
    
    // Refreshes all components to current Stock company and type
    public void refreshAllParts() throws ParseException {
        setStockMetaDataJsonDaily();
        setSMAMetaDataJsonDaily();
        setRSIMetaDataJsonDaily();
        
        setStockMetaDataJsonWeekly();
        setSMAMetaDataJsonWeekly();
        setRSIMetaDataJsonWeekly();
        
        setStockRefsDaily();
        setStockRefsWeekly();
        setSMARefsDaily();
        setSMARefsWeekly();
        setRSIRefsDaily();
        setRSIRefsWeekly();
        
        setTimeSeriesDailyObj();
        setTimeSeriesWeeklyObj();
        setSMAObjDaily();
        setSMAObjWeekly();
        setRSIObjDaily();
        setRSIObjWeekly();
        
        setTimeSeriesDaily();
        setTimeSeriesWeekly();
        setSMADaily();
        setSMAWeekly();
        setRSIDaily();
        setRSIWeekly();
        
        setDataOpenDaily(todayStock);
        setDataHighDaily(todayStock);
        setDataLowDaily(todayStock);
        setDataCloseDaily(todayStock);
        setDataVolumeDaily(todayStock);
        setDataSMADaily(todaySMA);
        setDataRSIDaily(todayRSI);
        
        setDataOpenWeekly(todayWeeklyStock);
        setDataHighWeekly(todayWeeklyStock);
        setDataLowWeekly(todayWeeklyStock);
        setDataCloseWeekly(todayWeeklyStock);
        setDataVolumeWeekly(todayWeeklyStock);
        setDataSMAWeekly(todayWeeklySMA);
        setDataRSIWeekly(todayWeeklyRSI);
    }
    
    // Stock: Apple
    // Types: TIME_SERIES_DAILY & SMA
    public void setStockJsonApple() throws IOException, ParseException {
        stockDailyJson = readJsonFromURL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL&interval=daily&outputsize=compact&apikey=VXA9OQNT36ZEX8KZ");
        smaDailyJson = readJsonFromURL("https://www.alphavantage.co/query?function=SMA&symbol=AAPL&interval=daily&time_period=60&series_type=open&apikey=VXA9OQNT36ZEX8KZ");
        rsiDailyJson = readJsonFromURL("https://www.alphavantage.co/query?function=RSI&symbol=AAPL&interval=daily&time_period=60&series_type=close&apikey=VXA9OQNT36ZEX8KZ"); // Uses time period 60
        
        stockWeeklyJson = readJsonFromURL("https://www.alphavantage.co/query?function=TIME_SERIES_WEEKLY&symbol=AAPL&apikey=VXA9OQNT36ZEX8KZ");
        smaWeeklyJson = readJsonFromURL("https://www.alphavantage.co/query?function=SMA&symbol=AAPL&interval=weekly&time_period=60&series_type=open&apikey=VXA9OQNT36ZEX8KZ");
        rsiWeeklyJson = readJsonFromURL("https://www.alphavantage.co/query?function=RSI&symbol=AAPL&interval=weekly&time_period=60&series_type=close&apikey=VXA9OQNT36ZEX8KZ");
        
        refreshAllParts();
    }
    
    // Stock: Microsoft
    // Types: TIME_SERIES_DAILY & SMA
    public void setStockJsonMicrosoft() throws IOException, ParseException {
        stockDailyJson = readJsonFromURL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=MSFT&interval=daily&outputsize=compact&apikey=VXA9OQNT36ZEX8KZ");
        smaDailyJson = readJsonFromURL("https://www.alphavantage.co/query?function=SMA&symbol=MSFT&interval=daily&time_period=60&series_type=open&apikey=VXA9OQNT36ZEX8KZ");
        rsiDailyJson = readJsonFromURL("https://www.alphavantage.co/query?function=RSI&symbol=MSFT&interval=daily&time_period=60&series_type=close&apikey=VXA9OQNT36ZEX8KZ"); // Uses time period 60
        
        stockWeeklyJson = readJsonFromURL("https://www.alphavantage.co/query?function=TIME_SERIES_WEEKLY&symbol=MSFT&apikey=VXA9OQNT36ZEX8KZ");
        smaWeeklyJson = readJsonFromURL("https://www.alphavantage.co/query?function=SMA&symbol=MSFT&interval=weekly&time_period=60&series_type=open&apikey=VXA9OQNT36ZEX8KZ");
        rsiWeeklyJson = readJsonFromURL("https://www.alphavantage.co/query?function=RSI&symbol=MSFT&interval=weekly&time_period=60&series_type=close&apikey=VXA9OQNT36ZEX8KZ");
        refreshAllParts();
    }
    
    // Create meta data objects for each API, throw an exception after 3 tries
    public void setStockMetaDataJsonDaily() {
        int count = 0;
        int max = 3;
        while (true){
            try{
            metadataJsonDaily = stockDailyJson.getJSONObject("Meta Data");
            break;
            } catch (ClassCastException exc) {
                if (++count == max) throw exc;
            }
        }
    }
    
    public void setSMAMetaDataJsonDaily() {
        int count = 0;
        int max = 3;
        while (true){
            try{
            smaMetaDataJsonDaily = smaDailyJson.getJSONObject("Meta Data");
            break;
            } catch (ClassCastException exc) {
                if (++count == max) throw exc;
            }
        }
    }
    
    public void setRSIMetaDataJsonDaily() {
        int count = 0;
        int max = 3;
        while (true){
            try{
            rsiMetaDataJsonDaily = rsiDailyJson.getJSONObject("Meta Data");
            break;
            } catch (ClassCastException exc) {
                if (++count == max) throw exc;
            }
        }
    }
    
    public void setStockMetaDataJsonWeekly() {
        int count = 0;
        int max = 3;
        while (true){
            try{
                metadataJsonWeekly = stockWeeklyJson.getJSONObject("Meta Data");
                break;
            } catch (ClassCastException exc) {
                if (++count == max) throw exc;
            }
        }
    }
    
    public void setSMAMetaDataJsonWeekly() {
        int count = 0;
        int max = 3;
        while (true){
            try{
            smaMetaDataJsonWeekly = smaWeeklyJson.getJSONObject("Meta Data");
            break;
            } catch (ClassCastException exc) {
                if (++count == max) throw exc;
            }
        }
    }
    
    public void setRSIMetaDataJsonWeekly() {
        int count = 0;
        int max = 3;
        while (true){
            try{
            rsiMetaDataJsonWeekly = rsiWeeklyJson.getJSONObject("Meta Data");
            break;
            } catch (ClassCastException exc) {
                if (++count == max) throw exc;
            }
        }
    }
    
    // Create data objects without meta data- these contain stock data
    public void setTimeSeriesDailyObj() {
        timeSeriesObj = stockDailyJson.getJSONObject("Time Series (Daily)"); // Dates and time series daily data in this object
    }
    
    public void setTimeSeriesWeeklyObj() {
        weeklyObj = stockWeeklyJson.getJSONObject("Weekly Time Series"); // Dates and time series daily data in this object
    }
    
    public void setSMAObjDaily() {
        smaDailyObj = smaDailyJson.getJSONObject("Technical Analysis: SMA"); // Dates and SMA data in this object
    }
    
    public void setSMAObjWeekly() {
        smaWeeklyObj = smaWeeklyJson.getJSONObject("Technical Analysis: SMA"); // Dates and SMA data in this object
    }
    
    public void setRSIObjDaily() {
        rsiDailyObj = rsiDailyJson.getJSONObject("Technical Analysis: RSI"); // Dates and RSI data in this object
    }
    
    public void setRSIObjWeekly() {
        rsiWeeklyObj = rsiWeeklyJson.getJSONObject("Technical Analysis: RSI"); // Dates and RSI data in this object
    }
    
    // Reads data and returns in string format
    private static String readAll(Reader readstr) throws IOException {
        StringBuilder string = new StringBuilder();
        int cp;
        while ((cp = readstr.read()) != -1) {
            string.append((char) cp);
        }
        return string.toString();
    }
    
    // Reads JSON objects from a URL using BufferedReader
    public static JSONObject readJsonFromURL(String url) throws IOException, JSONException {
        try (InputStream instream = new URL(url).openStream()) {
            BufferedReader read = new BufferedReader(new InputStreamReader(instream, Charset.forName("UTF-8")));
            String jsonText = readAll(read);
            JSONObject json = new JSONObject(jsonText);
            return json;
        }
    }
    
    // Gets date refreshed for each API
    public void setDateRefreshStockDaily() {
        String dateRefresh = metadataJsonDaily.getString("3. Last Refreshed"); // Gets date refreshed from meta data object
        try {
        dateRefreshedDaily = dateFormat.parse(dateRefresh); // Changes String dateRefresh into a Date object
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    public void setDateRefreshStockWeekly() {
        String dateRefresh = metadataJsonWeekly.getString("3. Last Refreshed"); // Gets date refreshed from meta data object
        try {
        dateRefreshedWeekly = dateFormat.parse(dateRefresh); // Changes String dateRefresh into a Date object
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    public void setDateRefreshSMADaily() throws ParseException{
        String smaDateRefresh = smaMetaDataJsonDaily.getString("3: Last Refreshed");
        try {
        dateRefreshedSMADaily = dateFormatSMA.parse(smaDateRefresh);
        } catch (JSONException e) {
            dateRefreshedSMADaily = dateFormat.parse(smaDateRefresh);
        } 
    }
    
    public void setDateRefreshSMAWeekly() throws ParseException {
        String smaDateRefresh = smaMetaDataJsonWeekly.getString("3: Last Refreshed");
        try {
        dateRefreshedSMAWeekly = dateFormatSMA.parse(smaDateRefresh);
        } catch (JSONException e) {
            dateRefreshedSMAWeekly = dateFormat.parse(smaDateRefresh);
        }
    }
    
    public void setDateRefreshRSIDaily() throws ParseException {
        String rsiDateRefresh = rsiMetaDataJsonDaily.getString("3: Last Refreshed");
        try {
        dateRefreshedRSIDaily = dateFormatRSI.parse(rsiDateRefresh);
        } catch (JSONException e) {
            dateRefreshedRSIDaily = dateFormat.parse(rsiDateRefresh);
        }
    }
    
    public void setDateRefreshRSIWeekly() throws ParseException {
        String rsiDateRefresh = rsiMetaDataJsonWeekly.getString("3: Last Refreshed");
        try {
        dateRefreshedRSIWeekly = dateFormatRSI.parse(rsiDateRefresh);
        } catch (JSONException e) {
            dateRefreshedRSIWeekly = dateFormat.parse(rsiDateRefresh);
            
        }
    }
    
    // Methods to take final data needed from APIs
    public void setDataOpenDaily(JSONObject todayStock) {
        dailyOpen = todayStock.getString("1. open");
    }
    
    public void setDataOpenWeekly(JSONObject todayWeeklyStock) {
        weeklyOpen = todayWeeklyStock.getString("1. open");
    }
    
    public void setDataHighDaily(JSONObject todayStock) {
        dailyHigh = todayStock.getString("2. high");
    }
    
    public void setDataHighWeekly(JSONObject todayWeeklyStock) {
        weeklyHigh = todayWeeklyStock.getString("2. high");
    }
    
    public void setDataLowDaily(JSONObject todayStock) {
        dailyLow = todayStock.getString("3. low");
    }
    
    public void setDataLowWeekly(JSONObject todayWeeklyStock) {
        weeklyLow = todayWeeklyStock.getString("3. low");
    }
    
    public void setDataCloseDaily(JSONObject todayStock) {
        dailyClose = todayStock.getString("4. close");
    }
    
    public void setDataCloseWeekly(JSONObject todayWeeklyStock) {
        weeklyClose = todayWeeklyStock.getString("4. close");
    }
    
    public void setDataVolumeDaily(JSONObject todayStock) {
        dailyVolume = todayStock.getString("5. volume");
    }
    
    public void setDataVolumeWeekly(JSONObject todayWeeklyStock) {
        weeklyVolume = todayWeeklyStock.getString("5. volume");
    }
    
    public void setDataSMADaily(JSONObject todaySMA) {
        dailySma = todaySMA.getString("SMA");
    }
    
    public void setDataSMAWeekly(JSONObject todayWeeklySMA) {
        weeklySma = todayWeeklySMA.getString("SMA");
    }
    
    public void setDataRSIDaily(JSONObject todayRSI) {
        dailyRsi = todayRSI.getString("RSI");
    }
    
    public void setDataRSIWeekly(JSONObject todayWeeklyRSI) {
        weeklyRsi = todayWeeklyRSI.getString("RSI");
    }
    
    // Sets current date and formats it
    public void setDateCurrent() {
        this.date = dateFormat.format(dateCurrent);
    }
    
    // Sets current stock company
    public void setCurrentStock(String currentStock) {
        this.currentStock = currentStock;
    }
    
    // Sets current date
    public String getDateCurrent() {
        return date;
    }
    
    // Gets date refreshed for Time Series (Daily)
    public Date getDateRefreshDaily() {
        return dateRefreshedDaily;
    }
    
    public Date getDateRefreshWeekly() {
        return dateRefreshedWeekly;
    }
    
    // Gets date refreshed for SMA
    public Date getDateRefreshedSMADaily() {
        return dateRefreshedSMADaily;
    }
    
    public Date getDateRefreshedSMAWeekly() {
        return dateRefreshedSMAWeekly;
    }
    
    // Gets date refreshed for RSI
    public Date getDateRefreshedRSIDaily() {
        return dateRefreshedRSIDaily;
    }
    
    public Date getDateRefreshedRSIWeekly() {
        return dateRefreshedRSIWeekly;
    }
    
    // Gets current stock company
    public String getCurrentStock() {
        return currentStock;
    }
    
    // Gets "open" string
    public String getOpenDaily() {
        return this.dailyOpen;
    }
    
    public String getOpenWeekly() {
        return this.weeklyOpen;
    }
    
    // Gets "high" string
    public String getHighDaily() {
        return this.dailyHigh;
    }
    
    public String getHighWeekly() {
        return this.weeklyHigh;
    }
    
    // Gets "low" string
    public String getLowDaily() {
        return this.dailyLow;
    }
    
    public String getLowWeekly() {
        return this.weeklyLow;
    }
    
    // Gets "close" string
    public String getCloseDaily() {
        return this.dailyClose;
    }
    
    public String getCloseWeekly() {
        return this.weeklyClose;
    }
    
    // Gets "volume" string
    public String getVolumeDaily() {
        return this.dailyVolume;
    }
    
    public String getVolumeWeekly() {
        return this.weeklyVolume;
    }
    
    // Gets "sma" string
    public String getSMADaily() {
        return this.dailySma;
    }
    
    public String getSMAWeekly() {
        return this.weeklySma;
    }
    
    // Gets "rsi" string
    public String getRSIDaily() {
        return this.dailyRsi;
    }
    
    public String getRSIWeekly() {
        return this.weeklyRsi;
    }
    
    public List<Double> listVals() {
        return this.vals;
    }
    
    public List<String> listDates() {
        return this.dates;
    }
    
    /*
        Adds data to the array used by graph
    */
    public void addPastWeekToArray(String typeOfData) {
        vals.clear();
        int attempt = 0;
        switch(typeOfData) {
            case "Open":
            attempt = 0;
                for (int i = 0; i < 8; i++) {
                    try{
                        todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                    }catch (JSONException e) {
                        try{
                            dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                            todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                        } catch (JSONException v) {
                            try {
                            dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                            todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                            } catch (JSONException s) {
                                System.out.println("Date not found.");
                            }
                        }
                    }
                    if (timeSeriesObj.has(dateFormat.format(dateRefreshedDaily)) && vals.size() < 7) {
                        String openFind = todayStock.getString("1. open");
                        vals.add(Double.parseDouble(openFind));
                        dates.add(dateFormat.format(dateRefreshedDaily));
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    }
                    else if (vals.size() < 7){
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                        i--;
                        attempt++;
                        if (attempt > 10) {
                            break;
                        }
                    }
                }

        case "High":
            attempt = 0;
                for (int i = 0; i < 8; i++) {
                    try{
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                    }catch (JSONException e) {
                        try {
                            dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                            todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                        }catch (JSONException ev) {
                            try {
                            dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                            todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                            } catch (JSONException s) {
                                System.out.println("Date not found.");
                            }
                        }
                    }
                    if (timeSeriesObj.has(dateFormat.format(dateRefreshedDaily)) && vals.size() < 7) {
                        String highFind = todayStock.getString("2. high");
                        vals.add(Double.parseDouble(highFind));
                        dates.add(dateFormat.format(dateRefreshedDaily));
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    }
                    else if (vals.size() < 7){
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                        i--;
                        attempt++;
                        if (attempt > 10) {
                            break;
                        }
                    }
                }
        case "Low":
            attempt = 0;

                for (int i = 0; i < 8; i++) {
                    try {
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                    } catch (JSONException e) {
                        try {
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                        todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                        }catch (JSONException ev) {
                            try {
                            dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                            todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                            } catch (JSONException s) {
                                System.out.println("Date not found.");
                            }
                        }
                    }
                    if (timeSeriesObj.has(dateFormat.format(dateRefreshedDaily)) && vals.size() < 7) {
                        String lowFind = todayStock.getString("3. low");
                        vals.add(Double.parseDouble(lowFind));
                        dates.add(dateFormat.format(dateRefreshedDaily));
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    }
                    else if (vals.size() < 7){
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                        i--;
                        attempt++;
                        if (attempt > 10) {
                            break;
                        }

                }
            }
        case "Close":
            attempt = 0;
            for (int i = 0; i < 8; i++) {
                try {
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                } catch (JSONException e) {
                    try {
                    dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                    }catch (JSONException ev) {
                        try {
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                        todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                        } catch (JSONException s) {
                            System.out.println("Date not found.");
                        }
                    }
                }
                if (timeSeriesObj.has(dateFormat.format(dateRefreshedDaily)) && vals.size() < 7) {
                    String closeFind = todayStock.getString("4. close");
                    vals.add(Double.parseDouble(closeFind));
                    dates.add(dateFormat.format(dateRefreshedDaily));
                    dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                }
                else if (vals.size() < 7){
                    dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    i--;
                    attempt++;
                    if (attempt > 10) {
                        break ;
                    }
                }
            }
        case "Volume":
            attempt = 0;
            for (int i = 0; i < 8; i++) {
                try {
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                } catch (JSONException e) {
                    try {
                    dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                    }catch (JSONException ev) {
                        try {
                        dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                        todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                        } catch (JSONException s) {
                            System.out.println("Date not found.");
                        }
                    }
                }
                if (timeSeriesObj.has(dateFormat.format(dateRefreshedDaily)) && vals.size() < 7) {
                    todayStock = timeSeriesObj.getJSONObject(dateFormat.format(dateRefreshedDaily));
                    String volumeFind = todayStock.getString("5. volume");
                    vals.add(Double.parseDouble(volumeFind));
                    dates.add(dateFormat.format(dateRefreshedDaily));
                    dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                }
                else if (vals.size() < 7){
                    dateRefreshedDaily = subtractDays(dateRefreshedDaily, 1);
                    i--;
                    attempt++;
                    if (attempt > 10) {
                        break ;
                    }
                }
            }
        case "SMA":
            attempt = 0;
            int num = 0;
            for (int i = 0; i < 8; i++) {
                try {
                    todaySMA = smaCurrentDailyObj.getJSONObject(dateFormatSMA.format(dateRefreshedSMADaily));

                    // Check if there are any values for the past week (7 days)
                    while (num < 7 && !smaDailyObj.has(dateFormatSMA.format(dateRefreshedSMADaily))) {
                        dateRefreshedSMADaily = subtractDays(dateRefreshedSMADaily, 1);
                        num++;
                    }
                } catch (JSONException e) {
                    try {
                        dateRefreshedSMADaily = subtractDays(dateRefreshedSMADaily, 1);
                    } catch(JSONException v) {
                    todaySMA = smaDailyObj.getJSONObject(dateFormat.format(dateRefreshedSMADaily));
                    todaySMA = smaCurrentDailyObj.getJSONObject(dateFormatSMA.format(dateRefreshedSMADaily));
                    }

                    // Checks if there are any values for the past week (7 days)
                    while (num < 7 && !smaDailyObj.has(dateFormat.format(dateRefreshedSMADaily))) {
                        dateRefreshedSMADaily = subtractDays(dateRefreshedSMADaily, 1);
                        num++;
                    }
                }
                if (smaCurrentDailyObj.has(dateFormat.format(dateRefreshedSMADaily)) && vals.size() < 7) {
                    String smaFind = todaySMA.getString("SMA");
                    vals.add(Double.parseDouble(smaFind));
                    dates.add(dateFormat.format(dateRefreshedDaily));
                    dateRefreshedSMADaily = subtractDays(dateRefreshedSMADaily, 1);
                    i--;
                    attempt++;
                    if (attempt > 10) {
                        break;
                    }
                }
            }
        case "RSI":
            attempt = 0;
            int num2 = 0;

            for (int i = 0; i < 8; i++) {
                try {
                    todayRSI = rsiCurrentDailyObj.getJSONObject(dateFormatRSI.format(dateRefreshedRSIDaily));
                    // Check if there are any values for the past week (7 days)
                    while (num2 < 7 && !smaDailyObj.has(dateFormatRSI.format(dateRefreshedRSIDaily))) {
                        dateRefreshedRSIDaily = subtractDays(dateRefreshedRSIDaily, 1);
                        num2++;
                    }
                } catch (JSONException e) {
                    try {
                        dateRefreshedRSIDaily = subtractDays(dateRefreshedRSIDaily, 1);
                    } catch(JSONException v) {
                    todayRSI = rsiDailyObj.getJSONObject(dateFormat.format(dateRefreshedRSIDaily));
                    todayRSI = rsiCurrentDailyObj.getJSONObject(dateFormatRSI.format(dateRefreshedRSIDaily));
                    }
                    // Check if there are any values for the past week (7 days)
                    while (num2 < 7 && !rsiDailyObj.has(dateFormat.format(dateRefreshedRSIDaily))) {
                        dateRefreshedRSIDaily = subtractDays(dateRefreshedRSIDaily, 1);
                        num2++;
                    }
                }
                if (rsiCurrentDailyObj.has(dateFormat.format(dateRefreshedRSIDaily)) && vals.size() < 7) {
                    String rsiFind = todayRSI.getString("RSI");
                    vals.add(Double.parseDouble(rsiFind));
                    dates.add(dateFormat.format(dateRefreshedDaily));
                    dateRefreshedRSIDaily = subtractDays(dateRefreshedRSIDaily, 1);
                    i--;
                    attempt++;
                    if (attempt > 10) {
                        break;
                    }
                }
            }
        }
    }
}
