package com.gitTask.gitusers.data.source.remote;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.gitTask.gitusers.Urls;
import com.gitTask.gitusers.data.GitUser;
import com.gitTask.gitusers.data.source.UsersDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rishi on 1/1/2018.
 */

public class UsersRemoteDataSource implements UsersDataSource {

    private static UsersRemoteDataSource INSTANCE;

    private static final String META_REL = "rel";
    private static final String META_FIRST = "first";
    private static final String META_LAST = "last";
    private static final String META_NEXT = "next";
    private static final String META_PREV = "prev";
    private static final String DELIM_LINKS = ","; //$NON-NLS-1$
    private static final String DELIM_LINK_PARAM = ";"; //$NON-NLS-1$
    private String first;
    private String last;
    private String next;
    private String prev;

    public static UsersRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UsersRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private UsersRemoteDataSource() {
    }

    @Override
    public void getUsers(@NonNull final LoadUsersCallback callback, final boolean forceUpdate) {
        String url = Urls.ROOT_URL + "users";
        makeRequest(url, callback);
//        callback.onUsersLoaded(new ArrayList<>(USERS_SERVICE_DATA.values()));
    }

    @Override
    public void getMoreUsers(@NonNull LoadUsersCallback callback) {
        String url = next;
        if (next != null) {
            makeRequest(url, callback);
        } else {
            callback.onDataNotAvailable("No More Users");
        }

    }

    @Override
    public void searchUsers(String searchTerm, LoadUsersCallback callback) {
        String url = Urls.ROOT_URL + "search/users?q=" + searchTerm;
        makeRequest(url, callback);
    }

    private void makeRequest(String url, final LoadUsersCallback callback) {
        OkHttpClient client = new OkHttpClient
                .Builder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onDataNotAvailable(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String linkHeader = response.header("Link");
                String responseString = response.body().string();
                ArrayList<GitUser> gitUsers = new ArrayList<>();
                try {
                    JSONArray jsonArray;
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        jsonArray = jsonObject.getJSONArray("items");
                    } catch (JSONException je) {
                        jsonArray = new JSONArray(responseString);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userJsonObject = jsonArray.getJSONObject(i);
                        GitUser gitUser = new Gson().fromJson(userJsonObject.toString(), GitUser.class);
                        gitUsers.add(gitUser);
                    }
                    callback.onUsersLoaded(gitUsers);
                    parseLinkHeaders(linkHeader);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onDataNotAvailable(response.code() + "\n" + response.message() + "\n" + (responseString != null ? responseString : ""));
                }
                Log.d("response", response.toString());
            }
        });

    }

    public void parseLinkHeaders(String linkHeader) {
        next = null;
        if (linkHeader != null) {
            String[] links = linkHeader.split(DELIM_LINKS);
            for (String link : links) {
                String[] segments = link.split(DELIM_LINK_PARAM);
                if (segments.length < 2)
                    continue;

                String linkPart = segments[0].trim();
                if (!linkPart.startsWith("<") || !linkPart.endsWith(">")) //$NON-NLS-1$ //$NON-NLS-2$
                    continue;
                linkPart = linkPart.substring(1, linkPart.length() - 1);

                for (int i = 1; i < segments.length; i++) {
                    String[] rel = segments[i].trim().split("="); //$NON-NLS-1$
                    if (rel.length < 2 || !META_REL.equals(rel[0]))
                        continue;

                    String relValue = rel[1];
                    if (relValue.startsWith("\"") && relValue.endsWith("\"")) //$NON-NLS-1$ //$NON-NLS-2$
                        relValue = relValue.substring(1, relValue.length() - 1);

                    if (META_FIRST.equals(relValue))
                        first = linkPart;
                    else if (META_LAST.equals(relValue))
                        last = linkPart;
                    else if (META_NEXT.equals(relValue))
                        next = linkPart;
                    else if (META_PREV.equals(relValue))
                        prev = linkPart;
                }
            }
        }
    }
}
