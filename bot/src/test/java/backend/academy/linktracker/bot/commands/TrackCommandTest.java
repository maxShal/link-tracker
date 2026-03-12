package backend.academy.linktracker.bot.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.properties.BotMessageProperties;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackCommandTest {
    private static final String TRACK_MESSAGE = "TRACK_MESSAGE";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMessageProperties botMessageProperties;

    @Mock
    private UserStateStorage userStateStorage;

    @InjectMocks
    private TrackCommand trackCommand;

    @Test
    void supportsShouldReturnTrueForTrack() {
        assertTrue(trackCommand.supports(Commands.TRACK));
    }

    @Test
    void handlerShouldSetWaitingTrackLinkStateAndSendMessage() {
        long chatId = 1L;
        when(botMessageProperties.getTrackLink()).thenReturn(TRACK_MESSAGE);

        trackCommand.handler(chatId, Commands.TRACK);

        verify(userStateStorage).setState(chatId, UserState.WAITING_TRACK_LINK);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertTrue(sendMessage.toWebhookResponse().contains(TRACK_MESSAGE));
    }
}
