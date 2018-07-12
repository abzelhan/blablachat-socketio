package com.dnd.slack;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bakhyt on 9/12/17.
 */
public class SlackWebhook {

    public static void sendMessage(String message) {
        sendMessage(message, null);
    }

    public static void sendMessage(String message, List<SlackAttachment> attachments) {
        String url = "https://hooks.slack.com/services/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";
        JSONObject jo = new JSONObject();
        jo.put("text", message);
        jo.put("mrkdwn", true);

        if (attachments != null && attachments.size() > 0) {
            JSONArray arr = new JSONArray();
            for (SlackAttachment attachment : attachments) {
                JSONObject j = new JSONObject();
                j.put("text", attachment.getText());
                if (attachment.getColor() != null && !attachment.getColor().isEmpty()) {
                    j.put("color", attachment.getColor());
                }
                if (attachment.getFallback() != null && !attachment.getFallback().isEmpty()) {
                    j.put("fallback", attachment.getFallback());
                }

                if (attachment.getTitle() != null && !attachment.getTitle().isEmpty()) {
                    j.put("title", attachment.getTitle());
                }

                if (attachment.getFields() != null && !attachment.getFields().isEmpty()) {
                    JSONArray arrFields = new JSONArray();
                    for (SlackAttachmentField attachmentField : attachment.getFields()) {
                        JSONObject jF = new JSONObject();
                        jF.put("title", attachmentField.getTitle());

                        if (attachmentField.getValue() != null && !attachmentField.getValue().isEmpty()) {
                            jF.put("value", attachmentField.getValue());
                        }

                        jF.put("short", attachmentField.isShort());
                        arrFields.put(jF);
                    }
                    j.put("fields", arrFields);
                }
                arr.put(j);
            }
            jo.put("attachments", arr);
        }

        //String result = WS.url(url).body("{\"text\":\""+message+"\",\"mrkdwn\": true}").post().getString();
        String json = jo.toString();
        System.out.println("slack->" + json);
        //String result = WS.url(url).body(json).post().getString();
        try {
            HttpResponse<String> result = Unirest.post(url).body(jo).asString();
            System.out.println("result: " + result.getBody());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
