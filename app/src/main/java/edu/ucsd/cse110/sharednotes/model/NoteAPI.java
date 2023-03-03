package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Note postNote(Note note) {
        var gson = new Gson();
        var json = gson.toJson(note);
        var req = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + note.title)
                .method("PUT", RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        Log.d("PUUUT", note.updatedAt + " vs curr " + System.currentTimeMillis());
        Log.d("PUUUUUUUUUUUT", json);
        try (var res = client.newCall(req).execute()) {
            var resJson = res.body().string();
            Log.d("TEST", "title: " + note.title);
            Log.d("TEST", "res: " + resJson);
            return Note.fromJSON(resJson);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public @Nullable Note getNote(String title) {
        var req = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();
        try (var res = client.newCall(req).execute()) {
            var resStr = res.body().string();
            var gson = new Gson();
            Map<String, ?> map = gson.fromJson(resStr, Map.class);
            if (map.containsKey("detail")) {
                return null;
            }
            return Note.fromJSON(resStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
