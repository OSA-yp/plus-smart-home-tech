package ru.yandex.practicum.commerce.shoppingcart.exception;

public class NotAuthorizedUserException extends RuntimeException {
    public NotAuthorizedUserException(String message) {
        super(message);
    }
}
