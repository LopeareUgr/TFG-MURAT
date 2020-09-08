package Utilities;

import com.eclipsesource.json.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import java.util.List;
/**
 *
 * @author Raúl López Arévalo
 */
public class JSON {
    // Construye el contenido con la key y el value
    public static String buildContent(String label, String cadena){
        JsonObject objeto = new JsonObject();
        objeto.add(label, cadena);
        return objeto.toString();
    }
    
    // Lee el contenido sea cual sea la key
    public static String readContent(String cadena) {
        JsonObject object = Json.parse(cadena).asObject();
        List<String> keys = object.names();
        String value = object.get(keys.get(0)).asString();
        return value;
    }
    
    public static void pintMessage(ACLMessage inbox){
        ////quitarSystem.out.println("Recibido en " + inbox.getReceiver(0) + inbox.getContent() + " de " + inbox.getSender());
        //quitarSystem.out.println("Sender: "+inbox.getSender()+" | Receiver"+inbox.getReceiver()+" | Content: " + inbox.getContent());
    }
    
    public static String content(String cadena){
        JsonObject objeto = new JsonObject();
        objeto.add("object", cadena);
        return objeto.toString();
    }
    public static String readContent(String label, String cadena) {
        JsonObject objeto = Json.parse(cadena).asObject();
        String value = objeto.get(label).asString();
        return value;
    }
    
    public static String readLabel(String cadena){
        JsonObject objeto = Json.parse(cadena).asObject();
        List<String> a = objeto.names();
        return a.get(0);
    }
}
