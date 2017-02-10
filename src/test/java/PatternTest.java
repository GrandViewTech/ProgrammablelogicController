import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest
	{
		
		public static void main(String[] args)
			{
				Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");
				Matcher matcher = pattern.matcher("${INPUT1}");
				int from = 0;
				int count = 0;
				while (matcher.find(from))
					{
						count++;
						from = matcher.start() + 1;
					}
				System.out.println("count " + count);
			}
			
	}
