package com.jforex.dzjforex.handler.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.LoginExecutor;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionLostException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;

@RunWith(HierarchicalContextRunner.class)
public class LoginHandlerTest extends CommonUtilForTest {

    private LoginExecutor loginHandler;

    @Mock
    private Authentification authentificationMock;
    @Mock
    private CredentialsFactory credentialsFactoryMock;
    @Mock
    private Zorro zorroMock;
    private int returnCode;

    @Before
    public void setUp() {
        when(credentialsFactoryMock.create(username,
                                           password,
                                           loginTypeDemo))
                                               .thenReturn(loginCredentials);

        loginHandler = new LoginExecutor(authentificationMock,
                                        credentialsFactoryMock,
                                        zorroMock);
    }

    private void callLogin() {
        returnCode = loginHandler.login(username,
                                        password,
                                        loginTypeDemo);
    }

    public class WhenAuthentificationIsOK {

        @Before
        public void setUp() {
            when(zorroMock.progressWait(any()))
                .thenReturn(ZorroReturnValues.LOGIN_OK.getValue());

            when(authentificationMock.login(loginCredentials))
                .thenReturn(Completable.complete());

            callLogin();
        }

        @Test
        public void returnValueIsLoginOK() {
            assertThat(returnCode, equalTo(ZorroReturnValues.LOGIN_OK.getValue()));
        }

        @Test
        public void progressWaitIsCalled() {
            verify(zorroMock).progressWait(any());
        }
    }

    @Test
    public void logoutCallsAuthentification() {
        when(authentificationMock.logout()).thenReturn(Completable.complete());

        returnCode = loginHandler.logout();

        assertThat(returnCode, equalTo(ZorroReturnValues.LOGOUT_OK.getValue()));
        verify(authentificationMock).logout();
    }

    public class WhenAuthentificationFailes {

        @Before
        public void setUp() {
            when(zorroMock.progressWait(any()))
                .thenReturn(ZorroReturnValues.LOGIN_FAIL.getValue());

            when(authentificationMock.login(loginCredentials))
                .thenReturn(Completable.error(new ConnectionLostException("Login fail!")));

            callLogin();
        }

        @Test
        public void returnValueIsLoginFAIL() {
            assertThat(returnCode, equalTo(ZorroReturnValues.LOGIN_FAIL.getValue()));
        }

        @Test
        public void authentificationIsCalledWithCredentials() {
            verify(authentificationMock).login(loginCredentials);
        }

        @Test
        public void progressWaitIsCalled() {
            verify(zorroMock).progressWait(any());
        }
    }
}
