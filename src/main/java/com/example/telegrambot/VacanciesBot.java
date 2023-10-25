package com.example.telegrambot;

import com.example.telegrambot.dto.VacancyDto;
import com.example.telegrambot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VacanciesBot extends TelegramLongPollingBot {
    @Autowired
    private VacancyService vacancyService;
    private  final Map<Long, String> lastShownVacancieLevel = new HashMap<>();
    public VacanciesBot() {
        super("6481501702:AAFZhem6tCfwnz-8DoOdY3l5ftp63uaSNHM");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.getMessage() != null) {
                handleStartCommand(update);
            }
            if (update.getCallbackQuery() != null) {
                String callbackData = update.getCallbackQuery().getData();
    System.out.println(callbackData);
                if      ("showJuniorVacancies".equals(callbackData)) {
                    showJuniorVacancies(update);
                }else if("showMiddleVacancies".equals(callbackData)) {
                    showMiddleVacancies(update);
                }else if("showSeniorVacancies".equals(callbackData)){
                    showSeniorVacancies(update);
                }else if (callbackData.startsWith("vacId=")) {
                    showVacancyDesc(  update,callbackData.split("=")[1]);
                }else if("backToVacanciesButton".equals(callbackData)) {
                     handleBackToVaccanciesCommand(update);
                }else if("backToStartMenu".equals(callbackData)) {
                    handleBackToStartCommand(update);
                }
            }
        }catch (Exception e){
            throw new RuntimeException("Can't send message to user");
        }
    }

    private void handleBackToStartCommand(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose title");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }

    private void handleBackToVaccanciesCommand(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String level = lastShownVacancieLevel.get(chatId);
        if ("junior".equals(level)){
            showJuniorVacancies(update);
        }else if ("middle".equals(level)){
            showMiddleVacancies(update);
        }else if ("senior".equals(level)){
            showSeniorVacancies(update);
        }
    }

    private  void showVacancyDesc(Update update ,String id) throws TelegramApiException {

        VacancyDto vacancy = vacancyService.get(id);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        String vacancyInfo = """
                *Title:* %s
                *Company:* %s
                *Short Description:* %s
                *Description:* %s
                *Salary:* %s
                *Link:* [%s](%s)
                """.formatted(
                        escapeMarkdownReservedChars(vacancy.getTitle()),
                escapeMarkdownReservedChars(vacancy.getCompany()),
                escapeMarkdownReservedChars(vacancy.getShortDesc()),
                escapeMarkdownReservedChars(vacancy.getLongDesc()),
                vacancy.getSalary().isBlank() ? "Not specified" : escapeMarkdownReservedChars(vacancy.getSalary()),
                "Click here for more details",
                escapeMarkdownReservedChars(vacancy.getLink())
        );

        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
        execute(sendMessage);

    }




    private String escapeMarkdownReservedChars(String text) {
        return text.replace("-","\\-")
                .replace("*","\\*")
                .replace("_","\\_")
                .replace("[","\\[")
                .replace("]","\\]")
                .replace("(","\\(")
                .replace(")","\\)")
                .replace("~","\\~")
                .replace("`","\\`")
                .replace(">","\\>")
                .replace("#","\\#")
                .replace("+","\\+")
                .replace(".","\\.")
                .replace("!","\\!");

    }

    private ReplyKeyboard getBackToVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to vacancies");
        backToVacanciesButton.setCallbackData("backToVacanciesButton");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenu = new InlineKeyboardButton();
        backToStartMenu.setText("Back to start menu");
        backToStartMenu.setCallbackData("backToStartMenu");
        row.add(backToStartMenu);

        InlineKeyboardButton chatGpt = new InlineKeyboardButton();
        chatGpt.setText("Get cover letter");
        chatGpt.setUrl("https://chat.openai.com/");
        row.add(chatGpt);

        return new InlineKeyboardMarkup(List.of(row));
    }
    private  ReplyKeyboard getVacanciesMenu(List<VacancyDto> vacancies){
        List<InlineKeyboardButton> row = new ArrayList<>();
        for(VacancyDto vacancy: vacancies){
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacId="+ vacancy.getId());
            row.add(vacancyButton);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

    private ReplyKeyboard getJuniorVacanciesMenu() {

        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();
        return getVacanciesMenu(vacancies);
    }
    private ReplyKeyboard getMiddleVacanciesMenu() {

        List<VacancyDto> vacancies = vacancyService.getMiddleVacanciesMenu();
        return getVacanciesMenu(vacancies);
    }
    private ReplyKeyboard getSeniorVacanciesMenu() {

        List<VacancyDto> vacancies = vacancyService.getSeniorVacanciesMenu();
        return getVacanciesMenu(vacancies);
    }
    private  void showJuniorVacancies(Update update ){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId =update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());


        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        lastShownVacancieLevel.put(chatId, "junior");
    }
    private void showMiddleVacancies(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId =update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());


        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        lastShownVacancieLevel.put(chatId, "middle");
    }
    private void showSeniorVacancies(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId =update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSeniorVacanciesMenu());


        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        lastShownVacancieLevel.put(chatId, "senior");
    }
    private void handleStartCommand(Update update){
        String text = update.getMessage().getText();
        System.out.println("received " + text);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Welcome to Dniwe bot Choose your destiny:");
        sendMessage.setReplyMarkup((getStartMenu()));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("Junior (newbie)");
        junior.setCallbackData("showJuniorVacancies");
        row.add(junior);

        InlineKeyboardButton middle = new InlineKeyboardButton();
        middle.setText("Middle ");
        middle.setCallbackData("showMiddleVacancies");
        row.add(middle);

        InlineKeyboardButton senior = new InlineKeyboardButton();
        senior.setText("Senior ");
        senior.setCallbackData("showSeniorVacancies");
        row.add(senior);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }
    @Override
    public String getBotUsername() {
        return "Mate79JavaNewbieBot";
    }
}