package com.example.raid_bot_2;

import com.example.raid_bot_2.oauth.Data;
import com.example.raid_bot_2.oauth.Dev;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatPermissions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetChatAdministratorsResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
@Service
public class RaidBot2Application {

//    @Autowired
    public static BotRepository botRepository;
    public static Request currentRequest = new Request();

    private static int step = 0;

    public RaidBot2Application(BotRepository botRepository){
        this.botRepository = botRepository;
    }

    public static TelegramBot bot = new TelegramBot("6863272879:AAGmjhIGqFhkxX9Rq9ZMsEPub7gpEnzFBcQ");

    static String  shieldMessage1 = "Locking chat and waiting for ";
    static String shieldMessage2 = "Please enter the twitter link: ";

    static Timer timer = new Timer();

    static TimerTask task;
    static boolean continueTask = false;

    static String sendingSUccess = """
    Locking chat until the tweet
    has %d likes, %d replies,
    %d reposts and %d bookmarks.

    Check the tweet here:
    %s
    """;
    public static void main(String[] args) {

        String welcomingMessage = """
            🔰 Instructions on using raidwork 🔰
                        
            1️⃣ Add @raidwork_bot to your Telegram group
                        
            2️⃣ MAKE THE BOT AN ADMIN.
               Must be Admin to function. Refer to screenshot above for permissions.
                        
            3️⃣ Only Admins can run the Shield Bot
                        
            4️⃣ To Start A Raid:
                        
               ➡️ Enter /shield,
                     Chat Locks
               ➡️ Follow onscreen prompts
                    
               ➡️ Enter /cancel to force stop current raid and unlock TG
            """;


        SpringApplication.run(RaidBot2Application.class, args);
        // Create your bot passing the token received from @BotFather
        // Register for updates
        bot.setUpdatesListener(updates -> {
            // ... process updates
            updates.forEach(update -> {

                if (update.message() != null && update.message().text() != null) {
                    String name = update.message().chat().type().name();
                    Long chatId1 = update.message().chat().id();
                    Long userId = update.message().from().id();
                    if (name.equals("Private")){
                        long chatId = update.message().chat().id();
                        SendResponse response = bot.execute(new SendMessage(chatId,"Only administrators can use this commands"));
                        return;
                    }
                    if (update.message().text().equals("/start")) {
                        long chatId = update.message().chat().id();
                        SendResponse response = bot.execute(new SendMessage(chatId, welcomingMessage));

                    }
                    if (update.message().text().equals("/shield") || update.message().text().equals("/shield@raidwork_bot")) {
                        if (!name.equals("Private") && adminCheck(chatId1,userId)){
                            long chatId = update.message().chat().id();
                            String firstName = update.message().from().firstName();
                            Integer messageId = update.message().messageId();
                            System.out.println(update.message());
                            startShieldProcess(chatId, firstName, messageId, update.message().chat().username());
                        }else {
                            System.out.println("only administrator");
                            long chatId = update.message().chat().id();
                            SendResponse response = bot.execute(new SendMessage(chatId,"Only administrators can use this commands").replyToMessageId(update.message().messageId()));
                            return;
                        }
                    }else{
                        handleUserInput(update, update.message().text(),update.message().chat().id());
                    }
                    if (update.message().text().equals("/cancel")) {
                        if (!name.equals("Private") && adminCheck(chatId1,userId)){
                            long chatId = update.message().chat().id();
                            SendResponse response = bot.execute(new SendMessage(chatId,"cancelling raid, unlock group"));
                            unlockGroup(chatId,update.message().chat().username());
//                        timer.cancel();
//                         Reset step for future requests
//                        continueTask = true;
//                            checkStats(chatId,update.message().chat().username());
                            stopTask();
                            System.out.println("cancel raid");
                            step = 0;
                            currentRequest = new Request();
                        }else {
                            long chatId = update.message().chat().id();
                            SendResponse response = bot.execute(new SendMessage(chatId,"Only administrators can use this commands").replyToMessageId(update.message().messageId()));
                            return;
                        }
                    }
                }
            });


            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
            // Create Exception Handler
        }, e -> {
            if (e.response() != null) {
                // got bad response from telegram
                e.response().errorCode();
                e.response().description();
            } else {
                // probably network error
                e.printStackTrace();
            }
        });

//        // Send messages
//        long chatId = update.message().chat().id();
//        SendResponse response = bot.execute(new SendMessage(chatId, "Hello!"));
    }


    private  static void startShieldProcess(long chatId, String firstName, Integer messageID, String username) {
        step = 1;
        try {
            SendResponse response = bot.execute(new SendMessage(chatId,
                    shieldMessage1 + firstName).replyToMessageId(messageID));
            SendResponse response2 =
                    bot.execute(new SendMessage(chatId, shieldMessage2).replyToMessageId(messageID));
            List<ChatMember> admin = getAdmin(chatId);
            lockGroup(admin, chatId, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleUserInput(Update update, String messageText, long chatId) {
        String groupUsername = update.message().chat().username();
        // Handle user input based on the current step
        switch (step) {
            case 1:
                // Process Twitter link
                if(!isValidTwitterLink(messageText)){
                    SendMessage errorsMessage = new SendMessage(chatId,"Invalid Twitter link. Please start over with /shield. again");
                    try {
                        bot.execute(errorsMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    step = 0; // Reset step
                    currentRequest = new Request();
                    unlockGroup(chatId, groupUsername);
                    return;
                }
                currentRequest.setTwitterLink(messageText);
                step++;
                SendMessage likesMessage = new SendMessage(chatId,"Please enter the number of likes required:");
                try {
                    bot.execute(likesMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                // Process likes
                if (!isInteger(messageText)){
                    SendMessage errorsMessage = new SendMessage(chatId,"Invalid Input. Enter a valid number of likes. Please start over with /shield. again");
                    step = 0; // Reset step
                    currentRequest = new Request();
                    try {
                        bot.execute(errorsMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    unlockGroup(chatId, groupUsername);
                    return;
                }
                step++;
                SendMessage repliesMessage = new SendMessage(chatId,"Please enter the number of replies required:");
                currentRequest.setLikes(Integer.parseInt(messageText));
                try {
                    bot.execute(repliesMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                // Process replies
                if (!isInteger(messageText)){
                    SendMessage errorsMessage = new SendMessage(chatId,"Invalid Input. Enter a valid number of replies. Please start over with /shield. again");
                    try {
                        bot.execute(errorsMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    unlockGroup(chatId, groupUsername);
                    step = 0; // Reset step
                    currentRequest = new Request();
                    return;
                }
                step++;
                SendMessage repostsMessage = new SendMessage(chatId,"Please enter the number of repost required:");
                currentRequest.setReplies(Integer.parseInt(messageText));
                try {
                    bot.execute(repostsMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                // Process reposts
                if (!isInteger(messageText)){
                    SendMessage errorsMessage = new SendMessage(chatId,"Invalid Input. Enter a valid number of repost. Please start over with /shield. again");
                    try {
                        bot.execute(errorsMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    step = 0; // Reset step
                    currentRequest = new Request();
                    unlockGroup(chatId, groupUsername);
                    return;
                }
                step++;
                SendMessage bookmarksMessage = new SendMessage(chatId,"Please enter the number of bookmarks required:");
                currentRequest.setRepost(Integer.parseInt(messageText));
                try {
                    bot.execute(bookmarksMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 5:
                // Process bookmarks
                if (!isInteger(messageText)){
                    SendMessage errorsMessage = new SendMessage(chatId,"Invalid Input. Enter a valid number of bookmarks. Please start over with /shield. again");
                    try {
                        bot.execute(errorsMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    step = 0; // Reset step
                    currentRequest = new Request();
                    unlockGroup(chatId, groupUsername);
                    return;
                }
                currentRequest.setBookmarks(Integer.parseInt(messageText));
                currentRequest.setId(generateRandomID(8));
                currentRequest.setFirstName(update.message().from().firstName());
                currentRequest.setFromId(update.message().from().id());
                currentRequest.setIdMessage(update.message().messageId());
                currentRequest.setDateTime(LocalDateTime.now());

                //persist current req
                Request save = botRepository.save(currentRequest);

                // success message
                String dynamicString = String.format(
                        sendingSUccess, currentRequest.getLikes(),
                        currentRequest.getReplies(), currentRequest.getRepost(),
                        currentRequest.getBookmarks(), currentRequest.getTwitterLink());
                try {
                    bot.execute(new SendMessage(chatId, dynamicString));
                }catch (Exception e){
                    e.printStackTrace();
                }
                scheduleTask(chatId, update.message().chat().username(), save);
                // Reset step for future requests
                step = 0;
                currentRequest = new Request();
                break;
            default:
//                unlockGroup(chatId, groupUsername);
                // Handle unexpected state
        }
    }

    private static boolean isValidTwitterLink(String messageText) {
        if (messageText.startsWith("https://")){
            return true;
        }else {
            return false;
        }
    }

    private static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String generateRandomID(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomID = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char randomChar = characters.charAt(random.nextInt(characters.length()));
            randomID.append(randomChar);
        }

        return randomID.toString();
    }

    public static List<ChatMember> getAdmin(long chatId){
        GetChatAdministratorsResponse execute = bot.execute(new GetChatAdministrators(chatId));
        List<ChatMember> administrators = execute.administrators();
        return administrators;
    }

    public static void lockGroup(List<ChatMember> adminsChatMembers, long chatId, String groupUsername){

        System.out.println("this from lock func");

        //change implementation on python
        List<Long> listofAdmin = adminsChatMembers.stream().map(chatMember -> chatMember.user().id()).toList();
        List<Long>listAllMember = new java.util.ArrayList<>(getUserId(groupUsername));

        boolean b = listAllMember.removeAll(listofAdmin);

        System.out.println(b + " from lock func");


        for (Long userId : listAllMember) {
            try {
                ChatPermissions permissions = new ChatPermissions();
                permissions.canSendMessages(false);
                BaseResponse response = bot.execute(
                        new RestrictChatMember(chatId, userId, permissions));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public static void unlockGroup(long chatId, String groupUsername){

        List<ChatMember> adminsChatMembers = getAdmin(chatId);

        //change implementation on python
        List<Long> listofAdmin = adminsChatMembers.stream().map(chatMember -> chatMember.user().id()).toList();
        List<Long>listAllMember = new java.util.ArrayList<>(getUserId(groupUsername));

        boolean b = listAllMember.removeAll(listofAdmin);

        for (Long userId : listAllMember) {
            try {
                ChatPermissions permissions = new ChatPermissions();
                permissions.canSendMessages(true);
                BaseResponse response = bot.execute(
                        new RestrictChatMember(chatId, userId, permissions));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private static List<Long> getUserId(String groupUsername)
    {
        final String uri = "https://py1-679b982ec058.herokuapp.com/users/"+groupUsername;

        RestTemplate restTemplate = new RestTemplate();
        User forObject = restTemplate.getForObject(uri, User.class);
        System.out.println("from restTemplate");
        List<Long> userId = forObject.getUserId();
        if (userId!=null){
            System.out.println(userId.get(0));
        }
        return userId;
    }

    public static void scheduleTask(long chatId, String groupName, Request request) {
        task = new TimerTask() {

            int count  =0;

            @Override
            public void run() {
                // Define the job to be performed
                count++;
                if (count <= 11){
                    boolean b = checkStats(chatId, groupName, request);
                }else {
                    this.cancel(); // Stop the task after 15 minutes
                    failCheckStats(chatId,groupName);
                    unlockGroup(chatId,groupName);
                    System.out.println("Cron job stopped after 15 minutes.");
                    count = 0;
//                            timer.cancel(); //
                }
            }
        };

        // Schedule the task to run at specific intervals
        // In this example, the job will run every 80 seconds
        timer.scheduleAtFixedRate(task, 0, 80000);

    }
    public static void stopTask() {
        try {
            task.cancel();
        }catch (Exception e){
            System.out.println(e.toString());
        }
        System.out.println("Cron job stopped manually.");
    }

    public static boolean checkStats(long chatId, String groupName, Request request2){

        int dbLikes, apiLikes, dbReplies,apiReplies,dbReposts,apiReposts, dbBookmarks, apiBookmarks;
        String tweetUrl;
        Request byDateTimeLatest ;

        if (request2==null){
            byDateTimeLatest = botRepository.findByDateTime();
            dbLikes = byDateTimeLatest.getLikes();
            dbReplies = byDateTimeLatest.getReplies();
            dbReposts = byDateTimeLatest.getRepost();
            dbBookmarks = byDateTimeLatest.getBookmarks();
            tweetUrl = byDateTimeLatest.getTwitterLink();
        }else {
            // req to db
            byDateTimeLatest = request2;
        }

        String s = extract_tweetU(byDateTimeLatest.getTwitterLink(),chatId);
        System.out.println(s + " from checks stats");

        String format = String.format("https://api.twitter.com/2/tweets/%s?tweet.fields=public_metrics", s);
        // req from api twitter

        System.out.println(format + "  :: from format");
        Data tweetDataResponse = httpOk(format);

        System.out.println( tweetDataResponse.getData().getPublic_metrics().getReply_count()+ " reply reply reply");


        apiLikes = tweetDataResponse.getData().getPublic_metrics().getLike_count();
        apiReplies = tweetDataResponse.getData().getPublic_metrics().getReply_count();
        apiReposts = tweetDataResponse.getData().getPublic_metrics().getRetweet_count();
        apiBookmarks = tweetDataResponse.getData().getPublic_metrics().getBookmark_count();


        Request request = new Request();
        request.setLikes(apiLikes);
        request.setReplies(apiReplies);
        request.setRepost(apiReposts);
        request.setBookmarks(apiBookmarks);


        if (areFieldsMatching(byDateTimeLatest, request)){
            LocalDateTime dateTime = byDateTimeLatest.getDateTime();

            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(dateTime, now);

            long minutes = duration.toMinutes();
            long seconds = duration.toSeconds() % 60;

            String message = "Likes: " + byDateTimeLatest.getLikes() + ", " + byDateTimeLatest.getReplies() + " replies, " + byDateTimeLatest.getReplies() + " reposts, " + byDateTimeLatest.getReplies() + " bookmarks.\n" +
                    "\n" +
                    "Overall Raid Stats:\n" +
                    "Likes: " + apiLikes + "\n" +
                    "Replies: " + apiReplies + "\n" +
                    "Repost: " + apiReposts + "\n" +
                    "Bookmarks: " + apiBookmarks + "\n" +
                    "\n" +
                    "The raid took " + minutes + " minutes and " + seconds + " seconds!\n" +
                    "\n" +
                    "Unlocking chat";


            try {
                bot.execute(new SendMessage(chatId, message));
            }catch (Exception e){
                e.printStackTrace();
            }
            stopTask();
            unlockGroup(chatId,groupName);
            return true;
        }
        String message = "Locking chat until the tweet has " + byDateTimeLatest.getLikes() + " likes, " + byDateTimeLatest.getReplies() + " replies, " + byDateTimeLatest.getRepost() + " reposts and " +   " bookmarks.\n"
                + "Current Likes: " + apiLikes + " | 🎯 " + byDateTimeLatest.getLikes() + "\n"
                + "Current Replies: " + apiReplies + " | 🎯 " + byDateTimeLatest.getReplies() + "\n"
                + "Current Reposts: " + apiReposts + " | 🎯 " + byDateTimeLatest.getRepost() + "\n"
                + "Current Bookmarks: " + apiBookmarks + " | 🎯 " + byDateTimeLatest.getBookmarks()  + "\n"
                + "Check the tweet here:\n" + byDateTimeLatest.getTwitterLink();
        try {
            bot.execute(new SendMessage(chatId, message));
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    private static String extract_tweetU(String stringUrl, long chatId ) {
        System.out.println(stringUrl + "from extract tweet");
            String regex = ".*twitter\\.com/.+?/status/(\\d+).*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(stringUrl);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            //test
            return null;
    }

    public static boolean failCheckStats(long chatId, String groupName){

        int dbLikes, apiLikes, dbReplies,apiReplies,dbReposts,apiReposts, dbBookmarks, apiBookmarks;
        String tweetUrl;

        // req to db
        Request byDateTimeLatest = botRepository.findByDateTime();
        dbLikes = byDateTimeLatest.getLikes();
        dbReplies = byDateTimeLatest.getReplies();
        dbReposts = byDateTimeLatest.getRepost();
        dbBookmarks = byDateTimeLatest.getBookmarks();
        tweetUrl = byDateTimeLatest.getTwitterLink();

        String message = "The raid has gone on for 15 minutes without the tweet reaching " +
                dbLikes + " likes, " + dbReplies + " replies, " + dbReposts + " reposts and " +
                dbBookmarks + " bookmarks.\n\n" +
                "Check the tweet here:\n" + tweetUrl + "\n\n" +
                "Unlocking Chat";
        try {
            bot.execute(new SendMessage(chatId, message));
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    static boolean areFieldsMatching(Request byDateTimeLatest, Request apiRequest) {
        return apiRequest.getLikes() >= byDateTimeLatest.getLikes() &&
                apiRequest.getReplies() >= byDateTimeLatest.getReplies() &&
                apiRequest.getRepost() >= byDateTimeLatest.getRepost() &&
                apiRequest.getBookmarks() >= byDateTimeLatest.getBookmarks();

    }

    static boolean adminCheck(long chatId, long userId){
        List<ChatMember> admin = getAdmin(chatId);
        List<Long> listAdmin = admin.stream().map(chatMember -> chatMember.user().id()).toList();
        boolean contains = listAdmin.contains(userId);
        return contains;
    }

    static Data httpOk(String url){
        Dev dev = new Dev();
        Data tweet = dev.executeGetRequest(url);
        return tweet;
    }

}
