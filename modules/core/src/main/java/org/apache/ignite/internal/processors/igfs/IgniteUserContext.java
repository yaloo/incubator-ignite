package org.apache.ignite.internal.processors.igfs;

import org.apache.ignite.*;
import org.apache.ignite.internal.util.typedef.internal.*;

import javax.security.auth.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 */
public abstract class IgniteUserContext {

    private static final IgniteUserContext instance = new AccessControllerIgniteUserContext();

    /**
     *
     * @param user
     * @param callable
     * @param <T>
     * @return
     * @throws IgniteCheckedException
     */
    public abstract <T> T doAs0 (String user, final Callable<T> callable) throws IgniteCheckedException;

    /**
     *
     * @return
     */
    public abstract String getContextUser0();

    /**
     *
     * @param user
     * @param callable
     * @param <T>
     * @return
     * @throws IgniteCheckedException
     */
    public static <T> T doAs(String user, final Callable<T> callable) throws IgniteCheckedException {
        return instance.doAs0(user, callable);
    }

    /**
     *
     * @return
     */
    public static String getContextUser() {
        return instance.getContextUser0();
    }

    private static final class IgnitePrincipal implements Principal {

        private final String name;

        public IgnitePrincipal(String name) {
            this.name = name.intern();
        }

        @Override public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            else if (o == null || getClass() != o.getClass())
                return false;
            else
                //noinspection StringEquality
                return name == ((IgnitePrincipal)o).name;
        }

        @Override public int hashCode() {
            return name.hashCode();
        }

        @Override public String toString() {
            return name;
        }
    }

    static class AccessControllerIgniteUserContext extends IgniteUserContext {
        /** {@inheritDoc} */
        @Override public <T> T doAs0(String user, final Callable<T> callable) throws IgniteCheckedException {
            user = user.intern();

            try {
                //noinspection StringEquality
                if (getContextUser0() == user)
                    return callable.call();

                Subject subject = new Subject();

                subject.getPrincipals().add(new IgnitePrincipal(user));

                return Subject.doAs(subject, new PrivilegedExceptionAction<T>() {
                    @Override public T run() throws Exception {
                        return callable.call();
                    }
                });
            } catch (Exception pae) {
                throw U.cast(pae);
            }
        }

        /** {@inheritDoc} */
        @Override public String getContextUser0() {
            AccessControlContext context = AccessController.getContext();

            Subject subject = Subject.getSubject(context);

            Set<IgnitePrincipal> set = subject.getPrincipals(IgnitePrincipal.class);

            if (set.isEmpty())
                return null;
            else
                return set.iterator().next().getName();
        }
    }

    static class ThreadLocalIgniteUserContext extends IgniteUserContext {
        private final ThreadLocal<Stack<String>> userStackThreadLocal = new ThreadLocal<Stack<String>>() {
            @Override protected Stack<String> initialValue() {
                return new Stack<>();
            }
        };

        /** {@inheritDoc} */
        @Override public <T> T doAs0(String user, Callable<T> callable) throws IgniteCheckedException {
            user = user.intern();

            final Stack<String> stack = userStackThreadLocal.get();

            try {
                //noinspection StringEquality
                if (!stack.isEmpty() && stack.peek() == user)
                    return callable.call(); // correct context is already there

                stack.push(user);

                try {
                    return callable.call();
                }
                finally {
                    String userPopped = stack.pop();
                    //noinspection StringEquality
                    assert user == userPopped;
                }
            } catch (Exception e) {
                throw U.cast(e);
            }
        }

        /** {@inheritDoc} */
        @Override public String getContextUser0() {
            Stack<String> stack = userStackThreadLocal.get();

            if (stack.isEmpty())
                return null;

            return stack.peek();
        }
    }
}
