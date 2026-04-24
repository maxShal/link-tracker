package backend.academy.linktracker.scrapper.senders;

import backend.academy.linktracker.scrapper.model.LinkForSend;

public interface ISendUpdate {
    void send(LinkForSend linkForSend);
}
