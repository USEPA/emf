package gov.epa.emissions.framework.client.meta.versions;

public class Utils {

    public static String addBreaks(String string, int lineLength, char lineBreak) {

        String retVal = string;

        try {

            StringBuilder sb = new StringBuilder();

            char[] charArray = string.toCharArray();
            int count = 0;
            for (int i = 0; i < charArray.length;) {

                char c = charArray[i++];
                sb.append(c);
                if (c == lineBreak) {
                    count = 0;
                } else {

                    if (++count >= lineLength) {

                        count = 0;

                        while (i < charArray.length) {

                            c = charArray[i++];

                            if (c != ' ' && c != '\t' && c != lineBreak) {
                                sb.append(c);
                            } else {
                                break;
                            }
                        }

                        if (i < charArray.length) {
                            sb.append(lineBreak);
                        }
                    }
                }
            }

            retVal = sb.toString();
        } catch (Exception e) {

            System.out.println("Error while adding breaks:");
            e.printStackTrace();
        }

        return retVal;
    }

    public static String convertTextToHTML(String string) {

        String retVal = string;

        try {

            StringBuilder sb = new StringBuilder();

            sb.append("<html>");

            String html = string.replaceAll("\n", "<br>");

            sb.append(html);

            sb.append("</html>");

            retVal = sb.toString();
        } catch (Exception e) {

            System.out.println("Error while converting text to html:");
            e.printStackTrace();
        }

        return retVal;
    }

    public static void main(String[] args) {

        String string = Utils
                .addBreaks(
                        "This is a test of a as really long line with more than 80 characters, so it should put a break in, the problem is is that it's going to put breaks in the middle of words. ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd This is a problem that needs to be addressed.",
                        80, '\n');
        System.out.print(string);
    }
}
