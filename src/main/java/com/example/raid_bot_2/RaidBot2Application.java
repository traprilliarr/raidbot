package com.example.raid_bot_2;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatPermissions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetChatAdministratorsResponse;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@SpringBootApplication
@Service
public class RaidBot2Application {

//    @Autowired
    public static BotRepository botRepository;
    public static Request currentRequest = new Request() ;

    private static int step = 0;

    public RaidBot2Application(BotRepository botRepository){
        this.botRepository = botRepository;
    }

    public static TelegramBot bot = new TelegramBot("6751969432:AAEevuR1LtG2j13kMOU6uI7Y-K_krGELGxw");

    static String  shieldMessage1 = "Locking chat and waiting for ";
    static String shieldMessage2 = "Please enter the twitter link: ";

    static String sendingSUccess = """
    Locking chat until the tweet
    has %d likes, %d replies,
    %d reposts and %d bookmarks.

    Check the tweet here:
    %s
    """;
    public static void main(String[] args) {

        String welcomingMessage = """
            🔰 Instructions on using SHIELD 🔰
                        
            1️⃣ Add @ChatterShield_Bot to your Telegram group
                        
            2️⃣ MAKE THE BOT AN ADMIN.
               Must be Admin to function. Refer to screenshot above for permissions.
                        
            3️⃣ Only Admins can run the Shield Bot
                        
            4️⃣ To Start A Raid:
                        
               ➡️ Enter /shield,
                     Chat Locks
               ➡️ Follow onscreen prompts
                        
               ➡️ Enter /end to cancel prompts
                        
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
                    if (update.message().text().equals("/start")) {
                        long chatId = update.message().chat().id();
                        SendResponse response = bot.execute(new SendMessage(chatId, welcomingMessage));

                    }
                    if (update.message().text().equals("/shield") || update.message().text().equals("/shield@tes_h_bot")) {
                        long chatId = update.message().chat().id();
                        String firstName = update.message().from().firstName();
                        Integer messageId = update.message().messageId();
                        System.out.println(update.message());
                        startShieldProcess(chatId, firstName, messageId);
                    }else{
                        handleUserInput(update, update.message().text(),update.message().chat().id());
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


    private  static void startShieldProcess(long chatId, String firstName, Integer messageID) {
        step = 1;

        try {
            SendResponse response = bot.execute(new SendMessage(chatId,
                    shieldMessage1 + firstName).replyToMessageId(messageID));
            SendResponse response2 =
                    bot.execute(new SendMessage(chatId, shieldMessage2).replyToMessageId(messageID));
            List<ChatMember> admin = getAdmin(chatId);
            lockGroup(admin, chatId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleUserInput(Update update, String messageText, long chatId) {
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
                    currentRequest = null;
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
                    currentRequest = null;
                    try {
                        bot.execute(errorsMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                    step = 0; // Reset step
                    currentRequest = null;
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
                    currentRequest = null;
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
                    currentRequest = null;
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

                // Reset step for future requests
                step = 0;
                currentRequest = null;
                break;
            default:
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

    public static void lockGroup(List<ChatMember> chatMembers, long chatId){
        ChatPermissions chatPermissions = new ChatPermissions();
        chatPermissions.canSendMessages(false);

        List<ChatMember> regularMembers = chatMembers.stream()
                .filter(member -> !chatMembers.contains(member))
                .collect(Collectors.toList());

        // GetAllMember() -->  gada
        // [
        //  userId : 99239932,
//             userId : 99239932,
//                     userId : 99239932
        // ]

        // methodInPython --> solusinya

        // for loop
        //  RestrictChatMember(user )


        System.out.println("s");
        for (ChatMember chatMember : regularMembers) {
            Long id = chatMember.user().id();
            RestrictChatMember restrictChatMember = new RestrictChatMember(chatId, id, chatPermissions);
            try {
                BaseResponse execute = bot.execute(restrictChatMember);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
