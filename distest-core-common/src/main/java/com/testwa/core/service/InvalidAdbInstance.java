package com.testwa.core.service;public class InvalidAdbInstance extends RuntimeException {    private static final long serialVersionUID = 1L;    public InvalidAdbInstance(String message, Throwable t) {        super(message, t);    }}