package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.model.LinkForUpdateCheck;
import backend.academy.linktracker.scrapper.model.response.LinkUpdateResponse;

public interface ISendUpdate {
    void send(LinkUpdateResponse latestUpdate, LinkForUpdateCheck link);
}
