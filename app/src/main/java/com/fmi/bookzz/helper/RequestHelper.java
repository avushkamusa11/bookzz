package com.fmi.bookzz.helper;
import android.util.Log;

import com.fmi.bookzz.entity.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class RequestHelper {
    //Links
    public static final String GET_ALL_BOOKS = "book/all/%s"; //token
    public static final String ADD_TO_READ = "myLibrary/%s/%s/%s"; //myLibrary/bookId/status/token
    public static final String GET_BOOK = "book/%s"; //bookId

    public static final String GET_COMMENTS = "comments/%s"; //bookId

    public static final  String ADDRESS = "http://192.168.63.35";
    public static final String PORT = "8081";
    public static final String GET_PDF = "book/pdf/%s"; // bookId
    public static final String GET_MY_BOOKS = "myLibrary/myBooks/%s"; // token
    public static final String UPDATE_STATUS = "myLibrary/%s/%s/%s";//{bookId}/{status}/{token}
    public static final String UPLOAD_PICTURE = "file/uploadProfileImage/%s";//token
    public static final String RATE_BOOK = "book/rate/%s/%s"; ///{bookId}/{grade};
    public static final String GET_ALL_USERS = "user/all/%s"; //token
    public static final String ADD_TO_FRIEND = "user/addFriend/%s/%s" ;//{friendId}/{token}
    public static final String GET_ALL_FRIENDS = "user/getFriends/%s";//token
    public static final String GET_USERS_BOOKS = "myLibrary/books/%s";// userId
    public static final String GET_YEAR_GOAL = "goal/myGoal/%s/%s"; // year/token
    public static final String ADD_GOAL = "goal/%s/%s"; //countOfBooks/token
    public static final String GET_READ_BOOKS = "myLibrary/readBooks/%s/%s"; //year/token
    public static final String ADD_SCHEDULE = "schedule/add/%s"; //token
    public static final String CHECK_SCHEDULE = "schedule/check/%s"; //token
    public static final String ADD_PLAN = "schedule/addPlan/%s";// date/startTime/title/token
    public static final String GET_PLANS = "schedule/getPlans/%s"; // token
    public static final String GET_STATISTIC = "statistic/get/%s/%s/%s"; //token
    public static final String ADD_QUOTE = "quotes/add/%s"; //token
    public static final String GET_MY_QUOTES = "quotes/myQuotes/%s";//tokrn
    public static final String UPDATE_QUOTE = "quotes/%s/%s";//quoteId/isPrivate
    public static final String GET_QUOTES = "quotes/all";
    public static final String LOGOUT = "logout/%s"; //token
    public static final String SEND_MESSAGE = "conversation/add/%s"; //token
    public static final String GET_MESSAGES = "conversation/getOneConversation/%s/%s";//receiverId/token

    public static String token;

    public static User currentUser;

    public static void requestService(String urlString,String requestMethod, ResponseListener listener){
        new Thread(() ->
        {
            HttpURLConnection connection = null;

            try{
                URL url = new URL(urlString);
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod(requestMethod);

                InputStreamReader stream  = new InputStreamReader(connection.getInputStream());

                BufferedReader reader = new BufferedReader(stream);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null){
                    sb.append(line);
                }

                reader.close();
                String response = sb.toString();

                listener.onResponse(response);


            }catch (Exception e){
                Log.wtf("Went Boom", e.getMessage());
                listener.onError(e.getMessage());
            }finally {
                if(connection != null)
                    connection.disconnect();
            }

        }).start();
    }

}
