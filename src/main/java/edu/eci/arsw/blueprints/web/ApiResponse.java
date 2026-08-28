package edu.eci.arsw.blueprints.web;

public record ApiResponse<T>(int code, String message, T data) {
}
