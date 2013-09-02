package org.asmmr.common;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jun 24, 2006
 * Time: 9:57:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageParts {

    public static class HeaderField {

        public String Name;
        public Vector Values;

        public HeaderField() {
            Values = new Vector();
            Name = "Unknown";
        }

        public HeaderField(String name) {
            this.Name = name;
            Values = new Vector();
        }

        public HeaderField(String name, HeaderFieldValue value) {
            this.Name = name;
            Values = new Vector();
            addValue(value);
        }

        public void addValue(HeaderFieldValue value) {
            Values.add(value);
        }

        public void appendHeaderValues(HeaderField header) {
            Values.addAll(header.Values);
        }

        public void removeValue(HeaderFieldValue value) {
            Values.remove(value);
        }

        public HeaderFieldValue getValue(int index) {
            return (HeaderFieldValue) Values.get(index);
        }

        public String toString() {
            String result = Name + ": ";

            Iterator iterator = Values.iterator();
            while (iterator.hasNext()) {
                HeaderFieldValue tmp = (HeaderFieldValue) iterator.next();
                result += tmp.toString() + ",";
            }
            if (result.charAt(result.length() - 1) == ',')
                result = result.substring(0, result.length() - 1);

            return result;
        }

        public Parameter findParameter(String name) {

            Iterator iterator = Values.iterator();
            while (iterator.hasNext()) {
                HeaderFieldValue tmp = (HeaderFieldValue) iterator.next();
                Parameter tmp2;
                if ((tmp2 = tmp.findParameter(name)) != null)
                    return tmp2;
            }
            return null;
        }

    }

    public static class HeadersCollection {

        public Vector _headers;

        public HeadersCollection() {
            _headers = new Vector();
        }

        public void addHeader(HeaderField header) {
            Iterator iterator = _headers.iterator();
            while (iterator.hasNext()) {
                HeaderField tmp = (HeaderField) iterator.next();
                if (tmp.Name.compareToIgnoreCase(header.Name) == 0) {
                    tmp.appendHeaderValues(header);
                    return;
                }
            }
            _headers.add(header);
        }

        public HeaderField getHeader(String name) {
            Iterator iterator = _headers.iterator();
            while (iterator.hasNext()) {
                HeaderField tmp = (HeaderField) iterator.next();
                if (tmp.Name.compareToIgnoreCase(name) == 0)
                    return tmp;
            }

            return null;//new HeaderField(name, new HeaderFieldValue());
        }

        public Parameter findParameter(String name) {

            Iterator iterator = _headers.iterator();
            while (iterator.hasNext()) {
                HeaderField tmp = (HeaderField) iterator.next();
                Parameter tmp2;
                if ((tmp2 = tmp.findParameter(name)) != null)
                    return tmp2;
            }
            return null;
        }

        public String toString() {
            Iterator iterator = _headers.iterator();
            String temp = "";
            while (iterator.hasNext()) {
                HeaderField tmp = (HeaderField) iterator.next();
                temp += tmp.toString() + "\r\n";
            }
            return temp;
        }

        public void clear() {
            _headers.clear();
        }
    }

    public static class HeaderFieldValue {
        public String Value;
        public Vector Parameters;

        public HeaderFieldValue() {
            Parameters = new Vector();
            Value = "";
        }

        public HeaderFieldValue(String Value) {
            this.Value = Value;
            Parameters = new Vector();
        }

        public void addParameter(Parameter parameter) {
            Parameters.add(parameter);
        }

        public void removeParameter(Parameter parameter) {
            Parameters.remove(parameter);
        }

        public Parameter getParameter(int index) {
            return (Parameter) Parameters.get(index);
        }

        public String toString() {
            String result = Value;

            Iterator iterator = Parameters.iterator();
            while (iterator.hasNext()) {
                Parameter tmp = (Parameter) iterator.next();
                result += ";" + tmp.toString();
            }
            return result;
        }

        public Parameter findParameter(String name) {

            Iterator iterator = Parameters.iterator();
            while (iterator.hasNext()) {
                Parameter tmp = (Parameter) iterator.next();
                if (tmp.Name.compareToIgnoreCase(name) == 0)
                    return tmp;
            }
            return null;
        }
    }

    public static class Parameter {
        public String Name;
        public String Value;

        public Parameter() {
            this.Name = "Unknown";
            this.Value = "Unknown";
        }

        public Parameter(String Name) {
            this.Name = Name;
            if (this.Name.compareToIgnoreCase("") == 0) this.Name = "Unknown";
            this.Value = "Unknown";
        }

        public Parameter(String Name, String Value) {
            this.Name = Name;
            this.Value = Value;
        }

        public String toString() {
            return Name + "=" + Value;
        }
    }
}
