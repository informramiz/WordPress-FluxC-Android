package org.wordpress.android.fluxc.network.rest.wpcom.reader;

import android.support.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.commons.text.StringEscapeUtils;
import org.wordpress.android.fluxc.model.ReaderSiteModel;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.lang.reflect.Type;
import java.util.ArrayList;

class ReaderSearchSitesDeserializer implements JsonDeserializer<ReaderSearchSitesResponse> {
    @Override
    public ReaderSearchSitesResponse deserialize(JsonElement json,
                                                Type typeOfT,
                                                JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ReaderSearchSitesResponse response = new ReaderSearchSitesResponse();

        response.offset = getJsonInt(json, "offset");
        response.sites = new ArrayList<>();

        JsonArray jsonFeedsList = jsonObject.getAsJsonArray("sites");
        if (jsonFeedsList != null) {
            for (JsonElement jsonFeed : jsonFeedsList) {
                ReaderSiteModel site = new ReaderSiteModel();
                site.setSiteId(getJsonLong(jsonFeed, "blog_ID"));
                site.setFeedId(getJsonLong(jsonFeed, "feed_ID"));
                site.setSubscribeUrl(getJsonString(jsonFeed, "subscribe_URL"));
                site.setSubscriberCount(getJsonInt(jsonFeed, "subscribers_count"));
                site.setUrl(getJsonString(jsonFeed, "URL"));

                String title = getJsonString(jsonFeed, "title");
                if (title != null) {
                    site.setTitle(StringEscapeUtils.unescapeHtml4(title));
                }

                // parse the site meta data
                try {
                    JsonElement jsonMetaSite = jsonFeed.getAsJsonObject()
                                                       .getAsJsonObject("meta")
                                                       .getAsJsonObject("data")
                                                       .getAsJsonObject("site");
                    site.setFollowing(getJsonBoolean(jsonMetaSite, "is_following"));
                    String description = getJsonString(jsonMetaSite, "description");
                    if (description != null) {
                        site.setDescription(StringEscapeUtils.unescapeHtml4(description));
                    }

                    JsonElement jsonIcon = jsonMetaSite.getAsJsonObject().getAsJsonObject("icon");
                    site.setIconUrl(getJsonString(jsonIcon, "ico"));
                } catch (NullPointerException e) {
                    AppLog.e(T.API, "NPE parsing reader site metadata", e);
                }

                response.sites.add(site);
            }
        }

        return response;
    }

    private @Nullable String getJsonString(JsonElement jsonElement, String property) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has(property)) {
            return jsonObject.get(property).getAsString();
        }
        return null;
    }

    private boolean getJsonBoolean(JsonElement jsonElement, String property) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has(property)) {
            return jsonObject.get(property).getAsBoolean();
        }
        return false;
    }

    private int getJsonInt(JsonElement jsonElement, String property) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has(property)) {
            return jsonObject.get(property).getAsInt();
        }
        return 0;
    }

    private long getJsonLong(JsonElement jsonElement, String property) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has(property)) {
            return jsonObject.get(property).getAsLong();
        }
        return 0;
    }
}
