package app.dipaz.androidkeyboard;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class ServicioTask extends AsyncTask<Void,Void,String> {
    private Context httpContext;
    ProgressDialog progressDialog;
    public String resultadoapi="";
    public String linkrequesAPI="";
    public String ime="";
    public String text="";
    public String location="";

    public ServicioTask (Context ctx, String linkAPI, String ime, String text, String location)
    {
        this.httpContext=ctx;
        this.linkrequesAPI=linkAPI;
        this.ime=ime;
        this.text=text;
        this.location=location;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = null;

        String wsURL = linkrequesAPI;
        URL url = null;
        try {
            url = new URL(wsURL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            JSONObject parametrosPost = new JSONObject();
            parametrosPost.put("number", ime);
            parametrosPost.put("text", text);
            parametrosPost.put("location", location);
            urlConnection.setReadTimeout(1500);
            urlConnection.setConnectTimeout(6*10*1000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream os =urlConnection.getOutputStream();
            BufferedWriter write = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            write.write(getPostDataString(parametrosPost));
            write.flush();
            write.close();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode==HttpsURLConnection.HTTP_OK){
                BufferedReader in=new BufferedReader((new InputStreamReader(urlConnection.getInputStream())));
                StringBuffer sb = new StringBuffer("");
                String linea="";
                while ((linea=in.readLine())!=null){
                    sb.append(linea);
                    break;
                }
                in.close();
                result=sb.toString();
            }else{
                result = new String("Error: "+responseCode);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        resultadoapi=s;
        Toast.makeText(httpContext,resultadoapi,Toast.LENGTH_LONG);
    }

    public String getPostDataString(JSONObject params) throws  Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()){
            String key = itr.next();
            Object value = params.get(key);
            if(first){
                first = false;
            }else{
                result.append("&");
            }
            result.append(URLEncoder.encode(key,"UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(),"UTF-8"));
        }
        return result.toString();
    }

}