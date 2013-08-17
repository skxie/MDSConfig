import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



public class MDSConfig {

	public String originalInputFileName = null;
	public String[] handler = null;
	public int[][] values = null;
	public double minValue = Double.MAX_VALUE;
	public String removal = null;
	
	public MDSConfig(String originalInputFileName) {
		this.originalInputFileName = originalInputFileName;
	}
	
	public void genMDS() {
		
		try {
			//determine the number of nodes and read values
			BufferedReader bufferedReader = new BufferedReader(new FileReader(originalInputFileName));
			String currLine = bufferedReader.readLine();
			handler = currLine.split("\\^");
			int numberOfNodes = handler.length;
			int numberOfLines = 0;
			values = new int[numberOfNodes][numberOfNodes];
			while ((currLine = bufferedReader.readLine()) != null) {
				String[] splitedString2 = currLine.split("\\^");
				if (numberOfNodes != splitedString2.length) {
					System.out.println("The number of nodes mismatched with " + numberOfNodes +  ", " + splitedString2.length);
					return;
				}
				for (int i = 0; i < numberOfNodes; i++) {
					values[numberOfLines][i] = Integer.parseInt(splitedString2[i]);
				}
				numberOfLines++;
			}
			if (numberOfNodes != numberOfLines) {
				System.out.println("The number of nodes and lines mismatched with " + numberOfNodes +  ", " + numberOfLines);
				return;
			}
			List<Boolean> used = new ArrayList<Boolean>();
			for (int i = 1; i <= numberOfNodes-3; i++) {
				for (int j = 0; j < numberOfNodes; j++)
					used.add(false);
				setUsed(used, 0, 0, i);
				used.clear();
			}
			
			
		} catch(IOException e) {
			System.out.println("No such file.");
		}
		
	}
	
	public void setUsed(List<Boolean> used, int curr, int pos, int total) {
		if (curr == total) {
			try {
				String outputFile = originalInputFileName + ".";
				for (int i = 0; i < used.size(); i++) {
					if (used.get(i).booleanValue())
						outputFile += "1";
					else
						outputFile += "0";
				}
				outputFile += ".out";
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
				boolean flag = false;
				for (int i = 0; i < used.size(); i++) {
					if (used.get(i).booleanValue())
						continue;
					if (!flag) {
						bufferedWriter.write(handler[i]);
						flag = true;
					} else
						bufferedWriter.write("^" + handler[i]);
				}
				bufferedWriter.newLine();
				for (int i = 0; i < used.size(); i++) {
					if (used.get(i).booleanValue())
						continue;
					boolean head = false;
					for (int j = 0; j < used.size(); j++) {
						if (used.get(j).booleanValue())
							continue;
						if (!head) {
							bufferedWriter.write(String.valueOf(values[i][j]));
							head = true;
						} else
							bufferedWriter.write("^" + String.valueOf(values[i][j]));
					}
					bufferedWriter.newLine();
				}
				bufferedWriter.close();
				executeRscript(outputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
			}
		} else {
			for (int i = pos; i < used.size(); i++) {
				if (!used.get(i).booleanValue()) {
					used.set(i, true);
					setUsed(used, curr+1, i+1, total);
					used.set(i, false);
				}
			}
		}
	}
	
	public void executeRscript(String inputfile) {
		Process p;
		try {
			String outputfile = inputfile.replace(".out", ".jpg");
			String file = inputfile + " " + outputfile;
			p = Runtime.getRuntime().exec("Rscript RscriptUsingSmacof.r " + file);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			String val = new String();
			while ((line = input.readLine()) != null) {
				val = line;
			}
			input.close();
			double res = Double.parseDouble(val);
			String info = "The stress value of removal of nodes - ";
			String nodes = new String();
			String flag = inputfile.split("\\.")[2];
			for (int i = 0; i < flag.length(); i++) {
				if (flag.charAt(i) == '1') {
					nodes += handler[i] + ", ";
				}
			}
			info += nodes + "- is ";
			System.out.println(info + res);
			if (this.minValue > res) {
				this.minValue = res;
				this.removal = nodes;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getRemoval() {
		return this.removal;
	}
	
	public Double getMinValue() {
		return this.minValue;
	}
	
	public static void main(String[] args) {
		
		String originalFileName = args[0];
		
		MDSConfig mdsConfig = new MDSConfig(originalFileName);
		mdsConfig.genMDS();
		System.out.println("The minimum stress value is " + mdsConfig.getMinValue() + " with the removal of following nodes - " + mdsConfig.getRemoval() + ".");
	}

}
