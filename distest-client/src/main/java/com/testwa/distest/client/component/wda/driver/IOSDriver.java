package com.testwa.distest.client.component.wda.driver;

import com.google.common.collect.ImmutableMap;
import com.testwa.distest.client.component.wda.element.Element;
import com.testwa.distest.client.component.wda.element.IOSLocator;
import com.testwa.distest.client.component.wda.remote.RemoteResponse;
import com.testwa.distest.client.component.wda.remote.WDACommand;
import com.testwa.distest.client.component.wda.remote.WDACommandExecutor;
import com.testwa.distest.client.component.wda.remote.WebDriverAgentRunner;
import com.testwa.distest.client.component.wda.support.IOSDeploy;
import com.testwa.distest.client.component.wda.support.ResponseValueConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class IOSDriver implements Driver {

    private DriverCapabilities capabilities;
    private WebDriverAgentRunner wdaRunner;
    private CommandExecutor commandExecutor;
    private String sessionId;
    private String udid;


    public IOSDriver(DriverCapabilities capabilities) {
        this.capabilities = capabilities;
        this.udid = capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID);
        this.wdaRunner = new WebDriverAgentRunner(capabilities);
        this.wdaRunner.start();
        this.commandExecutor = new WDACommandExecutor(wdaRunner.getWdaUrl());
        getSession();
        log.info("[{}] iOS driver init success !", udid);
    }

    @Override
    public List<Element> findElements(IOSLocator locator) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.USING.getKey(), locator.getSelector().getKey());
        parameters.put(WDACommand.Parameter.VALUE.getKey(), locator.getValue());
        RemoteResponse response = execute(WDACommand.FIND_ELEMENTS, new EnumMap<>(WDACommand.Wildcard.class), parameters);
        return new ResponseValueConverter(response).toElementsList(commandExecutor);
    }

    @Override
    public Element findElement(IOSLocator locator) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.USING.getKey(), locator.getSelector().getKey());
        parameters.put(WDACommand.Parameter.VALUE.getKey(), locator.getValue());
        RemoteResponse response = execute(WDACommand.FIND_ELEMENT, new EnumMap<>(WDACommand.Wildcard.class), parameters);
        return new ResponseValueConverter(response).toElement(commandExecutor);
    }

    @Override
    public Element focused() {
        return new ResponseValueConverter(execute(WDACommand.GET_FOCUSED_ELEMENT)).toElement(commandExecutor);
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        Map<String, Object> parameters = new HashMap<>();
        List<String> keys = Arrays.stream(keysToSend)
                .map(String.class::cast)
                .map(k -> k.split(""))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        parameters.put(WDACommand.Parameter.VALUE.getKey(), keys);
        execute(WDACommand.SEND_KEYS, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public String getPageSource() {
        return (String) execute(WDACommand.GET_PAGE_SOURCE).getValue();
    }

    @Override
    public Dimension getWindowSize() {
        return new ResponseValueConverter(execute(WDACommand.GET_CURRENT_WINDOW_SIZE)).toDimension();
    }

    @Override
    public void installApp(String... appPath) {
        String path = appPath.length > 0 ? appPath[0] : capabilities.getCapability(DriverCapabilities.Key.APP_PATH);
        new IOSDeploy(capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID)).installApp(path);
    }

    @Override
    public void removeApp(String... bundleId) {
        String id = bundleId.length > 0 ? bundleId[0] : capabilities.getCapability(DriverCapabilities.Key.BUNDLE_ID);
        new IOSDeploy(capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID)).removeApp(id);
    }

    @Override
    public void launch(String... bundleId) {
        String id = bundleId.length > 0 ? bundleId[0] : capabilities.getCapability(DriverCapabilities.Key.BUNDLE_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.BUNDLE_ID.getKey(), id);
        parameters.put(WDACommand.Parameter.ARGUMENTS.getKey(), buildArgs());
        execute(WDACommand.LAUNCH, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public void terminate(String... bundleId) {
        String id = bundleId.length > 0 ? bundleId[0] : capabilities.getCapability(DriverCapabilities.Key.BUNDLE_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.BUNDLE_ID.getKey(), id);
        execute(WDACommand.TERMINATE, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public void activate(String... bundleId) {
        String id = bundleId.length > 0 ? bundleId[0] : capabilities.getCapability(DriverCapabilities.Key.BUNDLE_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WDACommand.Parameter.BUNDLE_ID.getKey(), id);
        execute(WDACommand.ACTIVATE, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public void quit() {
        sessionId = null;
        if(wdaRunner != null) {
            wdaRunner.stop();
        }

    }

    @Override
    public void input(String text) {
        Map<String, Object> parameters = ImmutableMap.of("value", text.split(""));
        execute(WDACommand.INPUT, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public void tap(Integer x, Integer y) {
        double scale = Double.parseDouble(capabilities.getCapability(DriverCapabilities.Key.SCALE));
        Map<String, Object> parameters = ImmutableMap.of("x", x*scale, "y", y*scale);
        execute(WDACommand.TAP, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public void tapAndHold(Integer x, Integer y, Float duration) {
        double scale = Double.parseDouble(capabilities.getCapability(DriverCapabilities.Key.SCALE));
        Map<String, Object> parameters = ImmutableMap.of("x", x*scale, "y", y*scale, "duration", duration);
        execute(WDACommand.TOUCH_AND_HOLD, new EnumMap<>(WDACommand.Wildcard.class), parameters);

    }

    @Override
    public void swipe(Integer x1, Integer y1, Integer x2, Integer y2, int duration) {
        double scale = Double.parseDouble(capabilities.getCapability(DriverCapabilities.Key.SCALE));
        Map<String, Object> parameters = ImmutableMap.of("fromX", x1*scale, "fromY", y1*scale, "toX", x2*scale, "toY", y2*scale, "duration", String.valueOf(duration));
        execute(WDACommand.SWIP, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public void home() {
        Map<String, Object> parameters = new HashMap<>();
        execute(WDACommand.HOME, new EnumMap<>(WDACommand.Wildcard.class), parameters);
    }

    @Override
    public <T> T getScreenshot(OutputImageType<T> outputType) {
        return outputType.convertFromBase64Png((String) execute(WDACommand.GET_WINDOW_SCREENSHOT).getValue());
    }

    @Override
    public RemoteResponse execute(String command, Map<WDACommand.Wildcard, String> wildcards, Map<String, ?> parameters) {
        Long startTime = System.currentTimeMillis();
        if(StringUtils.isBlank(sessionId)) {
            this.getSession();
        }
        Optional.ofNullable(sessionId).ifPresent(id -> wildcards.put(WDACommand.Wildcard.SESSION_ID, sessionId));
        RemoteResponse response = commandExecutor.execute(command, wildcards, parameters);
        Long endTime = System.currentTimeMillis();
        log.debug("[{}] WDA execute {}, cast: {}ms", udid, command, endTime - startTime);
        return response;
    }

    @Override
    public RemoteResponse execute(String command, Map<WDACommand.Wildcard, String> wildcards) {
        return this.execute(command, wildcards, new HashMap<>());
    }

    @Override
    public RemoteResponse execute(String command) {
        return this.execute(command, new EnumMap<>(WDACommand.Wildcard.class), new HashMap<>());
    }


    private void getSession() {
        RemoteResponse response = commandExecutor.execute(WDACommand.STATUS);
        this.sessionId = response.getSessionId();
        log.info("[{}] Get WebDriverAgent session sessionId: {}", udid, sessionId);
    }


    private void createSession() {
        log.info("[{}] Starting new WebDriverAgent session.", udid);
        log.debug("Capabilities: " + capabilities.toString());
        String command = WDACommand.NEW_SESSION;

        Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.APP_PATH)).ifPresent(path -> {
            new IOSDeploy(capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID)).installApp(path);
        });

        RemoteResponse response = execute(command, new EnumMap<>(WDACommand.Wildcard.class), buildDesiredCapabilities());
        this.sessionId = response.getSessionId();
    }

    private Map<String, Object> buildDesiredCapabilities() {
        Map<String, Object> desiredWrapper = new HashMap<>();
        Map<String, Object> desiredCaps = new HashMap<>();
        desiredCaps.put(WDACommand.Parameter.ARGUMENTS.getKey(), buildArgs());

        Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.BUNDLE_ID)).ifPresent(cap -> {
            desiredCaps.put(DriverCapabilities.Key.BUNDLE_ID.getKey(), cap);
        });

        desiredWrapper.put("desiredCapabilities", desiredCaps);

        return desiredWrapper;
    }

    private Map<String, Object> buildArgs() {
        Map<String, Object> args = new HashMap<>();
        Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.LANGUAGE)).ifPresent(cap -> {
            args.put("-AppleLanguages", cap);
            args.put("-NSLanguages", cap);
        });

        Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.LOCALE)).ifPresent(cap -> {
            args.put("-AppleLocale", cap);
        });
        return args;
    }
}
