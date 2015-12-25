/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.mellowtech.core.util;

//just added dummy 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Deprecated
public class DateUtils {

  public static SimpleDateFormat ISO8601_WIKI_FORMAT = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss'Z'");

  public static SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss");

  public static SimpleDateFormat RFC822_FORMAT = new SimpleDateFormat(
      "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

  public static SimpleDateFormat WIKI_FORMAT = new SimpleDateFormat(
      "yyyyMMddHHmmss");

  public static Date parseWikiISO8601(String dateString) throws ParseException {
    return ISO8601_WIKI_FORMAT.parse(dateString);
  }

  public static Date parseISO8601(String dateString) throws ParseException {
    return ISO8601_FORMAT.parse(dateString);
  }

  public static String formatWikiISO8601(Date d) {
    return ISO8601_WIKI_FORMAT.format(d);
  }

  public static Date parseWIKI(String dateString) throws ParseException {
    return WIKI_FORMAT.parse(dateString);
  }

  public static String formatWIKI(Date d) {
    return WIKI_FORMAT.format(d);
  }

}
