
package interfaces;

import java.util.List;

import interpreter.events.TokenEvent;
import utils.Email;

public interface IEmailEventListener {
    void onReceiveEmailEvent(List<Email> emails);


}
