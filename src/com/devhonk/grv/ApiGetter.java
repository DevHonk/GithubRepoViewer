package com.devhonk.grv;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.scene.control.ProgressBar;
import jfx.messagebox.MessageBox;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class ApiGetter {
    public static JsonArray getRepos(String user, ProgressBar bar) throws InexistingUserException, IOException {
        try {
            URL url = new URL(String.format("https://api.github.com/users/%s/repos", user));

            URLConnection connection = url.openConnection();




            int size = connection.getContentLength();

            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            String res = "";
            int i;
            int bytes = 0;  //COUNT THE BYTES
            while((i = bis.read()) != -1) {
                bar.setProgress(bytes / (double) size);
                res += (char) i;
                bytes++;//so we read another byte
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            bis.close();
            JsonArray jsonObject = Main.gson.fromJson(res, JsonArray.class);
            return jsonObject;
        } catch (FileNotFoundException e) {
            throw new InexistingUserException("User " + user + " don't exists(yet) !");
        }
    }
    public static JsonArray getCommits(String user, String repo, String branch) throws InexistingUserException, IOException {
        try {
            BufferedInputStream bis = new BufferedInputStream(new URL(String.format("https://api.github.com/repos/%s/%s/commits/%s", user, repo, branch)).openStream());
            String res = "";
            int i;
            while((i = bis.read()) != -1) {
                res += (char) i;
            }
            bis.close();
            JsonArray jsonObject = Main.gson.fromJson(res, JsonArray.class);

            return jsonObject;
        } catch (FileNotFoundException e) {
            throw new InexistingUserException("User " + user + " don't exists(yet) !");
        }
    }
    public static JsonArray getCommits(String user, String repo) throws InexistingUserException, IOException {
        try {
            BufferedInputStream bis = new BufferedInputStream(new URL(String.format("https://api.github.com/repos/%s/%s/commits?since=0000-01-01T00:00:00Z", user, repo)).openStream());
            String res = "";
            int i;
            while((i = bis.read()) != -1) {
                res += (char) i;
            }
            bis.close();
            JsonArray jsonObject = Main.gson.fromJson(res, JsonArray.class);

            return jsonObject;
        } catch (FileNotFoundException e) {
            throw new InexistingUserException("User " + user + " don't exists(yet) !");
        }
    }

    public static JsonArray getBranches(String user, String repo) throws InexistingUserException, IOException {
        try {
            BufferedInputStream bis = new BufferedInputStream(new URL(String.format("https://api.github.com/repos/%s/%s/branches", user, repo)).openStream());
            String res = "";
            int i;
            while((i = bis.read()) != -1) {
                res += (char) i;
            }
            bis.close();
            JsonArray jsonObject = Main.gson.fromJson(res, JsonArray.class);

            return jsonObject;
        } catch (FileNotFoundException e) {
            throw new InexistingUserException("User " + user + " don't exists(yet) !");
        }
    }

    public static String getReadMe(String user, String repo) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new URL(String.format("https://api.github.com/repos/%s/%s/readme", user, repo)).openStream());
            String resJSON = "";
            int i;
            while((i = bis.read()) != -1) {
                resJSON += (char) i;
            }
            bis.close();
            JsonObject jsonObject = Main.gson.fromJson(resJSON, JsonObject.class);
            String downloadReadMe = jsonObject.get("download_url").getAsString();
            bis = new BufferedInputStream(new URL(downloadReadMe + "").openStream());
            String res = "";
            i = 0;
            while((i = bis.read()) != -1) {
                res += (char) i;
            }
            bis.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
