package at.rihnet.rihnetlogistikmde.ui.login.data;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.ui.login.data.model.LoggedInUser;

import java.io.IOException;


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static final String TAG = "RIHNet";

    public Result<LoggedInUser> login(String username, String kennwort) {

        try {
            LoggedInUser user = CommunicationSql.getMitarbw(username.toUpperCase(), kennwort);
            if (user == null) {
                return new Result.Error(new IOException("Benutzer hat keine Berechtigung oder Kennwort ist falsch!"));
            } else {
                return new Result.Success<>(user);
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}