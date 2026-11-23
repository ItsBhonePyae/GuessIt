package com.guessit.actualapp.view;

/**
 * Sign-in screen. The shared {@link AuthView} shell renders the split ticket
 * card with the "Sign In" tab already selected.
 */
public class LoginView extends AuthView {

    public LoginView() {
        super(Mode.LOGIN);
    }
}
