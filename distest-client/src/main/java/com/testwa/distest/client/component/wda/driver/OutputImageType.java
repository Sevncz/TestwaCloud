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


import com.testwa.distest.client.component.wda.exception.WebDriverAgentException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public interface OutputImageType<T> {
    OutputImageType<String> BASE64 = base64Png -> base64Png;
    OutputImageType<byte[]> BYTES = base64Png -> Base64.getMimeDecoder().decode(base64Png);
    OutputImageType<File> FILE = base64Png -> {
        try {
            File tmpFile = File.createTempFile("capture", ".png");
            tmpFile.deleteOnExit();
            try(FileOutputStream stream = new FileOutputStream(tmpFile)) {
                stream.write(BYTES.convertFromBase64Png(base64Png));
            }
            return tmpFile;
        } catch (IOException e) {
            throw new WebDriverAgentException(e);
        }
    };

    T convertFromBase64Png(String var1);
}
