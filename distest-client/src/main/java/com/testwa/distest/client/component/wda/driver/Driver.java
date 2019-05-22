/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.testwa.distest.client.component.wda.driver;


import com.testwa.distest.client.component.wda.element.Element;
import com.testwa.distest.client.component.wda.element.IOSLocator;

import java.awt.*;
import java.util.List;

public interface Driver extends Screenshotable, CommandExecutor {

    /**
     * Find all elements within the current page using the given mechanism.
     *
     * @param locator The locating mechanism to use
     * @return A list of all {@link Element}s, or an empty list if nothing matches
     * @see IOSLocator
     */
    List<Element> findElements(IOSLocator locator);

    /**
     * Find the first {@link Element} using the given method.
     * The findElement(..) invocation will return a matching row, or try again repeatedly until
     * the configured timeout is reached.
     *
     * @param locator The locating mechanism
     * @return The first matching element on the current page
     * @throws WebDriverAgentException If no matching elements are found
     * @see IOSLocator
     * @see WebDriverAgentException
     */
    Element findElement(IOSLocator locator);

    /**
     * Returns {@link Element} with focuse square.
     *
     * @return focused element
     */
    Element focused();

    /**
     * Use this method to simulate typing into tv keyboard.
     *
     * @param keysToSend character sequence to send to the element
     */
    void sendKeys(CharSequence... keysToSend);

    /**
     * Get the source of the last loaded application page.
     *
     * @return The source of the current application page
     */
    String getPageSource();

    /**
     * Get the size of the application window frame.
     *
     * @return The current window size.
     */
    Dimension getWindowSize();

    /**
     * Install an app on the tv device.
     * If param is not present, capabilities value would be taken.
     *
     * @param appPath path to app to install.
     */
    void installApp(String... appPath);

    /**
     * Remove an app on the tv device.
     * If param is not present, capabilities value would be taken.
     *
     * @param bundleId bundle id of the app to remove.
     */
    void removeApp(String... bundleId);

    /**
     * Launch app on the tv device.
     * If param is not present, capabilities value would be taken.
     *
     * @param bundleId bundle id of the app to launch.
     */
    void launch(String... bundleId);

    /**
     * Terminate app on the tv device.
     * If param is not present, capabilities value would be taken.
     *
     * @param bundleId bundle id of the app to launch.
     */
    void terminate(String... bundleId);

    /**
     * Activate app on the tv device.
     * If param is not present, capabilities value would be taken.
     *
     * @param bundleId bundle id of the app to launch.
     */
    void activate(String... bundleId);

    /**
     * Quits the session.
     */
    void quit();

    void input(String text);

    void tap(Integer x, Integer y);

    void swipe(String x1, String y1, String x2, String y2, int duration);

    void home();

}
