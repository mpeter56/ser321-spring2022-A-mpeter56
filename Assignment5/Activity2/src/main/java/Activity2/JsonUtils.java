/**
  File: JsonUtils.java
  Author: Student in Fall 2020B
  Description: JsonUtils class in package taskone.
*/

package Activity2;

import org.json.JSONObject;

/**
 * Class: JsonUtils
 * Description: Json Utilities.
 */
public class JsonUtils {
    public static JSONObject fromByteArray(byte[] bytes) {
        String jsonString = new String(bytes);
        return new JSONObject(jsonString);
    }

    public static byte[] toByteArray(JSONObject object) {
        return object.toString().getBytes();
    }
}
