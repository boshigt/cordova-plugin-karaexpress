package kara.express.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.graphics.Color;
import android.graphics.Matrix;
import android.widget.Toast;
import android.pt.iccard.IcCard;
import android.pt.minilcd.MiniLcd;
import android.pt.nfc.Nfc;
import android.pt.scan.Scan;
import android.pt.mifare.Mifare;
import android.pt.printer.Printer;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.content.Intent;
import android.content.IntentFilter;

public class KaraExpressPlugin extends CordovaPlugin {

    public static CallbackContext callbackContext;
    // private static MiniLcd lcd = new MiniLcd();
    // private boolean isLcdOpen = false;

    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if ("toast".equals(action)) {
            toast(args.getString(0), callbackContext);
        } else if ("miniDisplay".equals(action)) {

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String response = MiniDisplay(args);
                        // Thread.sleep(10000);
                        KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, response));
                    } catch (JSONException ex) {
                        Toast.makeText(webView.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else if ("displayString".equals(action)) {
            MiniDisplayString(args.getInt(0), args.getInt(1), args.getString(2), callbackContext);
        } else if ("readNFC".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final String NFC_ID = ReadNFC();
                    KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, NFC_ID));
                }
            });
        } else if ("readMifare".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final String Mifare_ID = ReadMifare();
                    KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, Mifare_ID));
                }
            });
        } else if ("scanCode".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final String code = ScanCode();
                    KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, code));
                }
            });
        } else if ("readIMSI".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final String IMSI = ReadIMSI();
                    KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, IMSI));
                }
            });
        } else if ("getText".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Good job - Үйлчилгээ амжилттай"));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KaraExpressPlugin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } else if ("readPassport".equals(action)) {
            ReadIDCard(args.getInt(0), callbackContext);
        } else if ("testPrint".equals(action)) {
            TestPrint(callbackContext);
        } else if ("print".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String response = Print(args);
                        KaraExpressPlugin.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, response));
                    } catch (JSONException ex) {
                        Toast.makeText(webView.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            return false;
        }
        return true;
    }

    private void toast(String msg, CallbackContext callbackContext) {
        if (msg == null || msg.length() == 0) {
            callbackContext.error("Empty message!");
        } else {
            Toast.makeText(webView.getContext(), msg, Toast.LENGTH_LONG).show();
            callbackContext.success(msg);
        }
    }

    private String MiniDisplay(JSONArray args) throws JSONException {
        try {
            MiniLcd lcd = new MiniLcd();
            lcd.open();
            // lcd.displayString(x, y, 0x000, 0xfff, msg);
            // Color color = new Color(255, 255, 255);
            lcd.fullScreen(Color.rgb(255, 255, 255));

            for (int i = 0; i < args.length(); i++) {
                int x = args.getJSONObject(i).getInt("x");
                int y = args.getJSONObject(i).getInt("y");
                String value = args.getJSONObject(i).getString("value");
                if (args.getJSONObject(i).getString("type").equals("text")) {
                    lcd.displayString(x, y, Color.rgb(0, 0, 0), Color.rgb(255, 255, 255), " " + value);
                } else if (args.getJSONObject(i).getString("type").equals("image")) {

                    int imgHeight = 0;
                    int imgWidth = 0;

                    byte[] decodedBytes = Base64.decode(value, Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    if (args.getJSONObject(i).has("height") && args.getJSONObject(i).has("width")) {
                        imgHeight = args.getJSONObject(i).getInt("height");
                        imgWidth = args.getJSONObject(i).getInt("width");

                        int width = bm.getWidth();
                        int height = bm.getHeight();
                        float scaleWidth = ((float) imgWidth) / width;
                        float scaleHeight = ((float) imgHeight) / height;
                        // CREATE A MATRIX FOR THE MANIPULATION
                        Matrix matrix = new Matrix();
                        // RESIZE THE BIT MAP
                        matrix.postScale(scaleWidth, scaleHeight);

                        // "RECREATE" THE NEW BITMAP
                        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

                        lcd.displayPicture(x, y, resizedBitmap);
                    } else {
                        lcd.displayPicture(x, y, bm);
                    }
                }
            }

            // lcd.close();
            // isLcdOpen = false;
            return "success";
        } catch (Exception ex) {
            Toast.makeText(webView.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            // callbackContext.error(ex.getMessage());
        }
        return "failed";
    }

    private String Print(JSONArray args) throws JSONException {
        try {
            Printer print = new Printer();
            print.open();
            print.init();

            //List<String> arrText = args;
            for (int i = 0; i < args.length(); i++) {
                if (args.getJSONObject(i).getString("type").equals("text")) {
                    if (args.getJSONObject(i).has("fontsize")) {
                        print.setFontSize(args.getJSONObject(i).getInt("fontsize"));
                    } else {
                        print.setFontSize(0);
                    }
                    print.printString(args.getJSONObject(i).getString("value"));
                } else if (args.getJSONObject(i).getString("type").equals("qrcode")) {
                    int size = 5;
                    if (args.getJSONObject(i).has("size")) {
                        size = args.getJSONObject(i).getInt("size");
                    }
                    print.printQR(args.getJSONObject(i).getString("value"), size);
                } else if (args.getJSONObject(i).getString("type").equals("image")) {
                    byte[] decodedString = Base64.decode(args.getJSONObject(i).getString("value"), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    print.printPicture(decodedByte, decodedByte.getWidth(), decodedByte.getHeight());
                } else if (args.getJSONObject(i).getString("type").equals("blank")) {
                    print.printBlankLines(args.getJSONObject(i).getInt("value"));
                }
            }
            print.close();
            return "success";
        } catch (Exception ex) {
            Toast.makeText(webView.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        return "failed";
    }

    private void TestPrint(CallbackContext callbackContext) {
        try {
            Printer print = new Printer();
            print.open();
            print.init();
            print.printString("Мобикомын үйлчилгээг сонгосон");
            print.printString("      танд баярлалаа.");
            print.printBlankLines(30);
            print.printQR("218017212888023186715559275809887377310480807057248126710420005257888678256272429487260386517661194610755348976545890107538190434815369653949537844393729761062617538276829084812940579346269615331155801304627087662947257171004479541212113656508648288086973206803150092082052293042890968411893494097295922297259601028", 5);
            print.printBlankLines(100);
            print.close();
            callbackContext.success("success");
        } catch (Exception ex) {
            callbackContext.error("Empty message!");
        }
    }

    private void MiniDisplayString(int x, int y, String msg, CallbackContext callbackContext) {
        if (msg == null || msg.length() == 0) {
            callbackContext.error("Empty message!");
        } else {
            MiniLcd lcd = new MiniLcd();
            lcd.open();
            lcd.displayString(x, y, 0x000, 0xfff, msg);
            callbackContext.success(msg);
        }
    }

    private String ScanCode() {
        Scan scan = new Scan();  //Create Scan

        int ret = scan.open();   //open scan

        if (ret < 0) {

            return "open fail";
        } else {
            String string = scan.scan(3000);  //if 3 second not scan anything,stop scan and display "not scan any info!!!"           

            if (string == null) {
                return "not scan any info!!!";
            } else {
                return string;
            }
        }
    }

    private String ReadNFC() {
        byte[] data = new byte[1024];
        Nfc nfc = new Nfc();
        int ret = nfc.open();
        if (ret == 0) {
            ret = nfc.seek(data);
            nfc.close();
            if (ret < 0) {
                return "not find nfc";
            } else {
                String string1 = "";
                for (int i = 0; i < 4; i++) {
                    string1 += String.format("%02X", data[i]);//string1 += Integer.toHexString(data[i] & 0xff) + ",";
                }
                return string1;
            }
        } else {
            return "Open fail";
        }
    }

    private String ReadMifare() {
        byte[] data = new byte[1024];
        Mifare mifare = new Mifare();
        int ret = mifare.open();
        if (ret == 0) {
            ret = mifare.seek(data);
            mifare.close();
            if (ret < 0) {
                return "not find mifare";
            } else {
                String string1 = "";
                int len = (int) data[0];
                for (int i = 1; i <= len; i++) {
                    string1 += String.format("%02X", data[i]);//Integer.toHexString(data[i] & 0xff) + ",";
                }
                return string1;
            }
        } else {
            return "Open fail";
        }
    }

    //boolean opened = false;
    private String ReadIMSI() {
        try {
            IcCard icCard = new IcCard();
            int ret = icCard.open();
            if (ret == 0) {
                ret = icCard.seek();
                if (ret == 0) {
                    ret = icCard.activate();
                    if (ret == 0) {
                        byte[] b1 = new byte[]{
                            (byte) 0xA0, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, (byte) 0x00
                        };
                        byte[] b2 = new byte[]{
                            (byte) 0xA0, (byte) 0xA4, 0x00, 0x00, 0x02, 0x2F, (byte) 0xE2
                        };
                        byte[] b3 = new byte[]{
                            (byte) 0xA0, (byte) 0xB0, 0x00, 0x00, 0x0A
                        };
                        byte[] data = new byte[1024];
                        int len = icCard.exeAPDU(b1, b1.length, data);
                        if (len <= 0) {
                            icCard.close();
                            return "APDU fail-1";
                        }
                        len = icCard.exeAPDU(b2, b2.length, data);
                        if (len <= 0) {
                            icCard.close();
                            return "APDU fail-2";
                        }
                        len = icCard.exeAPDU(b3, b3.length, data);
                        if (len <= 0) {
                            icCard.close();
                            return "APDU fail-3";
                        }
                        String imsi = "";
                        for (int i = 0; i < len - 2; i++) {
                            imsi += String.format("%02X", data[i]);
                        }
                        icCard.move();
                        icCard.close();
                        return imsiParse(imsi);
                    } else {
                        icCard.close();
                        return "active failed";
                    }
                } else {
                    icCard.close();
                    return "not find iccard";
                }
            } else {
                icCard.close();
                return "open fail";
            }
        } catch (Exception ex) {
            return "read error";
        }
    }

    private String imsiParse(String data) {
        char[] arrayStr = (data).toCharArray();
        char val;
        for (int i = 0; i < arrayStr.length; i += 2) {
            val = arrayStr[i + 1];
            arrayStr[i + 1] = arrayStr[i];
            arrayStr[i] = val;
        }
        String ret = new String(arrayStr);
        return ret;
    }

    private void ReadIDCard(int IsReadPic, CallbackContext callbackContext) {
        try {
            IcCard icCard = new IcCard();
            int ret = icCard.open();
            if (ret == 0) {
                ret = icCard.seek();
                if (ret == 0) {
                    ret = icCard.activate();
                    if (ret == 0) {
                        byte[] b1 = new byte[]{
                            0x00,
                            (byte) 0xA4,
                            0x00,
                            0x00,
                            0x02,
                            0x3F,
                            0x00
                        };
                        byte[] b2 = new byte[]{
                            0x00,
                            (byte) 0xA4,
                            0x04,
                            0x00,
                            0x02,
                            0x49,
                            0x44
                        };
                        byte[] b3 = new byte[]{
                            0x00,
                            (byte) 0xA4,
                            0x02,
                            0x00,
                            0x02,
                            0x01,
                            0x01
                        };
                        byte[] b4 = new byte[]{
                            0x00,
                            (byte) 0xB0,
                            0x00,
                            0x08,
                            (byte) 0xFF
                        };
                        byte[] b5 = new byte[]{
                            0x00,
                            (byte) 0xB0,
                            0x01,
                            0x07,
                            (byte) 0xFF
                        };
                        byte[] data = new byte[1024];
                        byte[] r4 = new byte[1024];
                        byte[] r5 = new byte[1024];
                        int len = icCard.exeAPDU(b1, b1.length, data);
                        //System.out.println("R1: "+byteToString(data, len));
                        if (len <= 0) {
                            icCard.close();
                            callbackContext.error("APDU fail-1");
                            return;
                        }
                        len = icCard.exeAPDU(b2, b2.length, data);
                        //System.out.println("R2: "+byteToString(data, len));
                        if (len <= 0) {
                            icCard.close();
                            callbackContext.error("APDU fail-2");
                            return;
                        }
                        len = icCard.exeAPDU(b3, b3.length, data);
                        //System.out.println("R3: "+byteToString(data, len));
                        if (len <= 0) {
                            icCard.close();
                            callbackContext.error("APDU fail-3");
                            return;
                        }
                        int len4 = icCard.exeAPDU(b4, b4.length, r4);
                        //System.out.println("R4: "+byteToString(r4, len4));
                        if (len4 <= 0) {
                            icCard.close();
                            callbackContext.error("APDU fail-4");
                            return;
                        }
                        int len5 = icCard.exeAPDU(b5, b5.length, r5);
                        //System.out.println("R5: "+byteToString(r5, len5));
                        if (len5 <= 0) {
                            icCard.close();
                            callbackContext.error("APDU fail-5");
                            return;
                        }
                        byte[] c = new byte[len4 - 2 + len5 - 2];
                        System.arraycopy(r4, 0, c, 0, len4 - 2);
                        System.arraycopy(r5, 0, c, len4 - 2, len5 - 2);
                        JSONObject json = toJson(parse(c));
                        //readPic();
                        if (IsReadPic == 1) {
                            json.put("pic", readPic(icCard));
                        }
                        icCard.move();
                        icCard.close();
                        callbackContext.success(json.toString());
                    } else {
                        icCard.close();
                        callbackContext.error("active failed");
                    }
                } else {
                    icCard.close();
                    callbackContext.error("not find iccard");
                }
            } else {
                icCard.close();
                callbackContext.error("open fail");
            }
        } catch (Exception ex) {
            callbackContext.error("read error");
        }
    }

    private List<String> parse(byte[] c) throws Exception {
        List<String> ret = new ArrayList<String>();
        int tag = 0;
        int len = 12;
        int index = 0;
        while (true) {
            ret.add(new String(Arrays.copyOfRange(c, index, index + len), "UTF-8"));
            index += len;
            if (tag == 0x0d) {
                break;
            }
            tag = c[index];
            len = (((c[index + 1] << 8) | c[index + 2]) + 256) % 256;
            //            System.out.println("tag: " + tag + ", len: " + len);
            index += 3;
        }
        return ret;
    }

    private JSONObject toJson(List< String> array) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("register", array.get(0));
        json.put("birthdate", array.get(1));
        json.put("sex", array.get(2));
        json.put("name", array.get(3));
        json.put("surname", array.get(4));
        json.put("familyname", array.get(5));
        json.put("expiry", array.get(6));
        json.put("issuedate", array.get(7));
        json.put("issuer", array.get(8));
        json.put("location", array.get(9));
        json.put("qr", array.get(10));
        json.put("id", array.get(11));
        json.put("address", array.get(12));
        return json;
    }

    private String readPic(IcCard icCard) throws Exception {
        byte[] commandSelect;
        byte[] responseAPDU = new byte[1024];
        commandSelect = new byte[]{
            0x00,
            (byte) 0xA4,
            0x00,
            0x00,
            0x02,
            0x3F,
            0x00
        };
        //responseAPDU = channel.transmit(commandSelect);
        icCard.exeAPDU(commandSelect, commandSelect.length, responseAPDU);
        //System.out.println("APDU1: " + byteToString(commandSelect));
        //System.out.println("RES1: " + byteToString(responseAPDU, len));
        commandSelect = new byte[]{
            0x00,
            (byte) 0xA4,
            0x04,
            0x00,
            0x02,
            0x49,
            0x44
        };
        icCard.exeAPDU(commandSelect, commandSelect.length, responseAPDU);
        //System.out.println("APDU2: " + byteToString(commandSelect));
        //System.out.println("RES2: " + byteToString(responseAPDU, len));
        commandSelect = new byte[]{
            0x00,
            (byte) 0xA4,
            0x02,
            0x00,
            0x02,
            0x01,
            0x01
        };
        icCard.exeAPDU(commandSelect, commandSelect.length, responseAPDU);
        //System.out.println("APDU3: " + byteToString(commandSelect));
        //System.out.println("RES3: " + byteToString(responseAPDU, len));
        commandSelect = new byte[]{
            0x00,
            (byte) 0xA4,
            0x02,
            0x00,
            0x02,
            0x01,
            0x02
        };
        icCard.exeAPDU(commandSelect, commandSelect.length, responseAPDU);
        //System.out.println("APDU4: " + byteToString(commandSelect));
        //System.out.println("RES4: " + byteToString(responseAPDU, len));
        ByteArrayOutputStream picData1 = new ByteArrayOutputStream();
        ByteArrayOutputStream picData2 = new ByteArrayOutputStream();
        int j = 0x05;
        int i = 0x00;
        String sst = "";
        int p1_first = 0x00;
        int p2_first = 0x00;
        int p1_end = 0x00;
        int p2_end = 0x00;
        int count = 0;
        while (i <= 0x30) {
            count++;
            try {
                commandSelect = new byte[]{
                    0x00,
                    (byte) 0xB0,
                    (byte) i,
                    (byte) j,
                    (byte) 0xFF
                };
                //System.out.println("1st READPIC REQ: " + byteToString(commandSelect));
                int len = icCard.exeAPDU(commandSelect, commandSelect.length, responseAPDU);
                //System.out.println("1st READPIC RES: " + byteToString(responseAPDU, len-2));
                //if (responseAPDU.getData() != null) {
                if (len > 0) {
                    picData1.write(picDataCut(responseAPDU, len));
                } else {
                    p1_first = i - 1;
                    p2_first = j + 1;
                    while (true) {
                        j--;
                        commandSelect = new byte[]{
                            0x00,
                            (byte) 0xB0,
                            (byte) i,
                            (byte) j,
                            (byte) 0xFF
                        };
                        //System.out.println("2nd READPIC REQ: " + byteToString(commandSelect));
                        len = icCard.exeAPDU(commandSelect, commandSelect.length, responseAPDU);
                        //System.out.println("2nd READPIC RES: " + byteToString(responseAPDU, len-2));
                        if (len > 0) {
                            picData2.write(picDataCut(responseAPDU, len));
                            p1_end = i;
                            p2_end = j;
                            break;
                        } else {
                            //  break;
                        }
                        if (j <= 0x00) {
                            i--;
                            j = 0xFF;
                        }
                    }
                    break;
                }
            } catch (Exception ex) {
                throw ex;
            }
            j = j + 0xFF;
            if (j > 0xFF) {
                i++;
                j = j - 0x100;
            }
        }
        int len2 = 0;
        if (p1_first == p1_end) {
            if (p2_first > p2_end) {
                len2 = 255 - (p2_first - p2_end);
            } else {
                len2 = 255 - (p2_end - p2_first);
            }
        } else {
            if (p2_first > p2_end) {
                len2 = p2_first - p2_end - 1;
            } else {
                len2 = p2_end - p2_first - 1;
            }
        }
        picData2.writeTo(picData1);
        return byteToStringNoSpace(picData1.toByteArray());
    }

    private byte[] picDataCut(byte[] data, int len) {
        byte[] resData = new byte[len - 2];
        int ii = 0;
        for (int i = 0; i < len - 2; i++) {
            resData[ii++] = data[i];
        }
        return resData;
    }

    private String byteToStringNoSpace(byte[] data) {
        String str = "";
        for (int i = 0; i < data.length; i++) {
            str += String.format("%02X", data[i]);
        }
        return str;
    }
}
