/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.graql.template;

import ai.grakn.graql.Graql;
import ai.grakn.graql.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.junit.Rule;
import org.junit.Test;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.rules.ExpectedException;

import static ai.grakn.graql.Graql.parse;
import static junit.framework.TestCase.assertEquals;

public class MacroTest {


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void noescpMacroOneVarTest(){
        String template = "insert $this isa @noescp(<value>);";
        String expected = "insert $this0 isa whale;";

        Map<String, Object> data = Collections.singletonMap("value", "whale");

        assertParseEquals(template, data, expected);
    }

    @Test
    public void noescpMacroMultiVarTest(){
        String template = "insert $x has fn @noescp(<firstname>) has ln @noescp(<lastname>);";
        String expected = "insert $x0 has fn 4 has ln 5;";

        Map<String, Object> data = new HashMap<>();
        data.put("firstname", "4");
        data.put("lastname", "5");

        assertParseEquals(template, data, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noescpMacroBreaksWithWrongNumberArguments(){
        String template = "@noescp(<value>, <otherValue>)";
        Graql.parseTemplate(template, new HashMap<>());
    }

    // int macro

    @Test
    public void intMacroTest(){
        String template = "insert $x val @int(<value>);";
        String expected = "insert $x0 val 4;";

        assertParseEquals(template, Collections.singletonMap("value", "4"), expected);
        assertParseEquals(template, Collections.singletonMap("value", 4), expected);
    }

    @Test
    public void whenMacroIsWrongCase_ResolvedToLowerCase(){
        String template = "insert $x val @InT(<value>);";
        String expected = "insert $x0 val 4;";

        assertParseEquals(template, Collections.singletonMap("value", "4"), expected);
        assertParseEquals(template, Collections.singletonMap("value", 4), expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void intMacroBreaksWithWrongNumberArguments0(){
        assertParseEquals("@int()", Collections.emptyMap(), "");
    }

    // double macro

    @Test
    public void doubleMacroTest(){
        String template = "insert $x val @double(<value>);";
        String expected = "insert $x0 val 4.0;";

        assertParseEquals(template, Collections.singletonMap("value", "4.0"), expected);
        assertParseEquals(template, Collections.singletonMap("value", 4.0), expected);
    }

    @Test
    public void whenParsingDoubleInFrenchLocale_DontUseComma(){
        Locale.setDefault(Locale.FRANCE);
        String template = "insert $x val @double(<value>);";
        String expected = "insert $x0 val 4.0;";

        assertParseEquals(template, Collections.singletonMap("value", "4.0"), expected);
        assertParseEquals(template, Collections.singletonMap("value", 4.0), expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doubleMacroBreaksWithWrongNumberArguments(){
        assertParseEquals("@double()", Collections.emptyMap(), "");
    }

    // equals

    @Test
    public void equalsMacroTest(){
        String template = "insert $x val @equals(<this>, <that>);";
        String expected = "insert $x0 val true;";

        Map<String, Object> data = new HashMap<>();
        data.put("this", "50");
        data.put("that", "50");

        assertParseEquals(template, data, expected);

        template = "insert $x val @equals(<this>, <notThat>);";
        expected = "insert $x0 val false;";

        data = new HashMap<>();
        data.put("this", "50");
        data.put("notThat", "500");

        assertParseEquals(template, data, expected);

        template = "insert $x val @equals(<this>, <notThat>);";
        expected = "insert $x0 val false;";

        data = new HashMap<>();
        data.put("this", "50");
        data.put("notThat", 50);

        assertParseEquals(template, data, expected);

        template = "insert $x val @equals(<this>, <that>, <those>);";
        expected = "insert $x0 val true;";

        data = new HashMap<>();
        data.put("this", 50);
        data.put("that", 50);
        data.put("those", 50);

        assertParseEquals(template, data, expected);

        template = "insert $x val @equals(<this>, <that>, <notThat>);";
        expected = "insert $x0 val false;";

        data = new HashMap<>();
        data.put("this", 50);
        data.put("that", 50);
        data.put("notThat", 50.0);

        assertParseEquals(template, data, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void equalsMacroBreaksWithWrongNumberArguments(){
        assertParseEquals("@equals(<this>)", Collections.singletonMap("this", "one"), "");
    }

    @Test
    public void macroInArgumentTest(){
        String template = "if (@equals(<this>, <that>)) do { insert $this isa equals; } else { insert $this isa not; }";
        String expected = "insert $this0 isa equals;";
        Map<String, Object> data = new HashMap<>();
        data.put("this", "50");
        data.put("that", "50");

        assertParseEquals(template, data, expected);

        expected = "insert $this0 isa not;";
        data = new HashMap<>();
        data.put("this", "50");
        data.put("that", "500");

        assertParseEquals(template, data, expected);
    }

    @Test
    public void stringMacroTest(){
        String template = "insert $this val @string(<value>);";
        String expected = "insert $this0 val \"1000\";";

        Map<String, Object> data = Collections.singletonMap("value", 1000);

        assertParseEquals(template, data, expected);
    }

    @Test
    public void longMacroTest(){
        String template = "insert $x val @long(<value>);";
        String expected = "insert $x0 val 4;";

        assertParseEquals(template, Collections.singletonMap("value", "4"), expected);
        assertParseEquals(template, Collections.singletonMap("value", 4), expected);
    }

    @Test
    public void booleanMacroTest(){
        String template = "insert $x val @boolean(<value>);";
        String expected = "insert $x0 val true;";

        assertParseEquals(template, Collections.singletonMap("value", "true"), expected);
        assertParseEquals(template, Collections.singletonMap("value", "True"), expected);

        expected = "insert $x0 val false;";

        assertParseEquals(template, Collections.singletonMap("value", "false"), expected);
        assertParseEquals(template, Collections.singletonMap("value", "False"), expected);
    }

    @Test
    public void whenDateMacroCalledWithMoreThanTwoArguments_ExceptionIsThrown(){
        String template = "insert $x val @date(<date>, \"mm/dd/yyyy\", \"dd/mm/yyyy\");";

        exception.expect(IllegalArgumentException.class);

        Graql.parseTemplate(template, Collections.singletonMap("date", "10/09/1993"));
    }

    @Test
    public void whenDateMacroCalledWithDateFormat_DateFormatConvertedToISODateTime() throws Exception {
        String dateTimePattern = "MM/dd/yyyy";
        String dateAsString = "10/09/1993";

        String formattedTime = LocalDate
                .parse(dateAsString, DateTimeFormatter.ofPattern(dateTimePattern)).atStartOfDay()
                .format(DateTimeFormatter.ISO_DATE_TIME);

        String template = "insert $x val @date(<date>, \"" + dateTimePattern + "\");";
        String expected = "insert $x0 val " + formattedTime + ";";

        assertParseEquals(template, Collections.singletonMap("date", dateAsString), expected);
    }

    @Test
    public void whenDateMacroCalledWithDateTimeFormat_DateFormatConvertedToISODateTime() throws Exception {
        String dateTimePattern = "yyyy-MM-dd HH:mm";
        String dateAsString = "1986-04-08 12:30";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimePattern);

        String formattedTime = LocalDateTime
                .parse(dateAsString, formatter)
                .format(DateTimeFormatter.ISO_DATE_TIME);

        String template = "insert $x val @date(<date>, \"" + dateTimePattern + "\");";
        String expected = "insert $x0 val " + formattedTime + ";";

        assertParseEquals(template, Collections.singletonMap("date", dateAsString), expected);
    }

    @Test
    public void whenDateMacroCalledAndDateCannotBeParsed_ExceptionIsThrown(){
        String dateTimePattern = "dd-MMM-yyyy HH:mm:ss";
        String dateAsString = "03-feb-2014 01:16:31";

        String template = "insert $x val @date(<date>, \"" + dateTimePattern + "\");";

        exception.expect(DateTimeParseException.class);
        exception.expectMessage("Cannot parse date value");

        assertParseEquals(template, Collections.singletonMap("date", dateAsString), null);
    }

    @Test
    public void whenDateMacroCalledWithInvalidDateFormat_ExceptionIsThrown(){
        String template = "insert $x val @date(<date>, \"this is not a format\");";

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot parse date format");

        assertParseEquals(template, Collections.singletonMap("date", "10/09/1993"), null);
    }

    @Test
    public void stringToUpperCaseTest(){
        String template = "insert $this has something @upper(<value>);";
        String expected = "insert $this0 has something \"CAMELCASEVALUE\";";

        Map<String, Object> data = Collections.singletonMap("value", "camelCaseValue");
        assertParseEquals(template, data, expected);
    }

    @Test
    public void stringToLowerCaseTest(){
        String template = "insert $this has something @lower(<value>);";
        String expected = "insert $this0 has something \"camelcasevalue\";";

        Map<String, Object> data = Collections.singletonMap("value", "camelCaseValue");
        assertParseEquals(template, data, expected);
    }

    @Test
    public void splitMacroTest() {
        String template = "insert $x for (val in @split(<list>, \",\") ) do { has description <val>};";
        String expected = "insert $x0 has description \"orange\" has description \"cat\" has description \"dog\";";

        assertParseEquals(template, Collections.singletonMap("list", "cat,dog,orange"), expected);
    }

    private void assertParseEquals(String template, Map<String, Object> data, String expected){
        List<Query> result = Graql.parseTemplate(template, data);
        assertEquals(parse(expected), result.get(0));
    }
}
