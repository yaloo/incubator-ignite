package org.apache.ignite.igfs;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import java.util.concurrent.*;

/**
 * Provides ability to execute IGFS code in a context of a specific user.
 */
public abstract class IgfsUserContext {
    /**
     * Thread local to hold the current user context.
     */
    private static final ThreadLocal<String> userStackThreadLocal = new ThreadLocal<String>() {
        @Override protected String initialValue() {
            String dfltUser = FileSystemConfiguration.DFLT_USER_NAME;

            return dfltUser.intern();
        }
    };

    /**
     * Executes given callable in the given user context.
     * The main contract of this method is that {@link #currentUser()} method invoked
     * inside 'cllbl' callable always returns 'user' this callable executed with.
     * @param user the user name
     * @param cllbl the callable to execute
     * @param <T> The type of callable result.
     * @return the result of
     * @throws NullPointerException if 'user' or 'callble' is null
     * @throws IgniteException if any Exception thrown from callable.call().
     * The contract is that if this method throws IgniteException, this IgniteException getCause() method
     * must return exactly the Exception thrown from the callable.
     */
    public static <T> T doAs(String user, final Callable<T> cllbl) {
        user = user.intern();

        final String ctxUser = userStackThreadLocal.get();

        try {
            //noinspection StringEquality
            if (ctxUser == user)
                return cllbl.call(); // correct context is already there

            userStackThreadLocal.set(user);

            try {
                return cllbl.call();
            }
            finally {
                userStackThreadLocal.set(ctxUser);
            }
        } catch (Exception e) {
            throw new IgniteException(e);
        }
    }

    /**
     * Gets the current context user.
     * If this method is invoked outside of any doAs(), it will return the default user
     * name as defined in {@link FileSystemConfiguration#DFLT_USER_NAME}.
     * Note that the returned user name is always interned, so
     * you may compare the names using '==' reference equality.
     * @return the current user, never null.
     */
    public static String currentUser() {
        return userStackThreadLocal.get();
    }
}
