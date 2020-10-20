package smo_system;

import configs.SimulationConfig;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Analyzer
{
  private class Results
  {
    public boolean isSource;
    public int number = 0;

    public int requestCount = 0;
    public double rejectProbability = 0;
    public double lifeTime = 0;
    public double bufferTime = 0;
    public double processTime = 0;
    public double bufferTimeDispersion = 0;
    public double processTimeDispersion = 0;

    public double usageRate = 0;

    Results(boolean isSource)
    {
      this.isSource = isSource;
    }
  }


  private final Simulator simulator;
  private final ArrayList<Results> sourcesResult;
  private final ArrayList<Results> processorsResult;

  private final NumberFormat formatter = new DecimalFormat("#0.000");
  private final double Ta = 1.643;
  private final double d = 0.1;
  private final boolean debug = true;

  public Analyzer(Simulator simulator)
  {
    this.simulator = simulator;
    this.sourcesResult = new ArrayList<>();
    this.processorsResult = new ArrayList<>();
  }

  public Analyzer(String fileName)
  {
    this.simulator = new Simulator(fileName);
    this.sourcesResult = new ArrayList<>();
    this.processorsResult = new ArrayList<>();
  }

  public void analyze(boolean simulated)
  {
    if (!simulated)
    {
      simulator.startSimulation(false);
    }
    analyzeSources();
    analyzeProcessors();
//    printResults();
  }

  public ArrayList<ArrayList<String>> getSourceResults()
  {
    ArrayList<ArrayList<String>> ar = new ArrayList<>();
    for (Results r : sourcesResult)
    {
      ArrayList<String> s = new ArrayList<>();
      s.add(String.valueOf(r.number));
      s.add(String.valueOf(r.requestCount));
      s.add(formatter.format(r.rejectProbability));
      s.add(formatter.format(r.lifeTime));
      s.add(formatter.format(r.bufferTime));
      s.add(formatter.format(r.processTime));
      s.add(formatter.format(r.bufferTimeDispersion));
      s.add(formatter.format(r.processTimeDispersion));
      ar.add(s);
    }
    return ar;
  }

  public ArrayList<ArrayList<String>> getProcessorResults()
  {
    ArrayList<ArrayList<String>> ar = new ArrayList<>();
    for (Results r : processorsResult)
    {
      ArrayList<String> s = new ArrayList<>();
      s.add(String.valueOf(r.number));
      s.add(formatter.format(r.usageRate));
      ar.add(s);
    }
    return ar;
  }

  private void analyzeSources()
  {
    ProductionManager pm = simulator.getProductionManager();
    ArrayList<Source> sources = pm.getSources();
    ArrayList<ArrayList<Request>> rejected = pm.getRejectedRequests();
    SelectionManager sm = simulator.getSelectionManager();
    ArrayList<ArrayList<Request>> success = sm.getSuccessRequests();
    for (Source s : sources)
    {
      Results r = new Results(true);
      r.number = s.getNumber();
      r.requestCount = s.getRequestCount();
      r.rejectProbability = ((double) rejected.get(r.number).size()) / ((double) r.requestCount);
      r.lifeTime =
        success.get(r.number).stream().mapToDouble(a -> (a.getTimeInProcessor() + a.getTimeInBuffer())).sum() /
        r.requestCount;
      r.bufferTime = success.get(r.number).stream().mapToDouble(Request::getTimeInBuffer).sum() / r.requestCount;
      r.processTime = success.get(r.number).stream().mapToDouble(Request::getTimeInProcessor).sum() / r.requestCount;
      r.bufferTimeDispersion = success.get(r.number).stream().mapToDouble(
        a -> Math.pow((a.getTimeInBuffer() - r.bufferTime), 2) / (r.requestCount - 1)).sum();
      r.processTimeDispersion = success.get(r.number).stream().mapToDouble(
        a -> Math.pow((a.getTimeInProcessor() - r.processTime), 2) / (r.requestCount - 1)).sum();
      sourcesResult.add(r);
    }
  }

  private void analyzeProcessors()
  {
    SelectionManager sm = simulator.getSelectionManager();
    ArrayList<Processor> processors = sm.getProcessors();
    double endTime = simulator.getEndTime();

    for (Processor p : processors)
    {
      Results r = new Results(false);
      r.number = p.getNumber();
      r.usageRate = p.getWorkTime() / endTime;
      processorsResult.add(r);
    }
  }

  public int analyzeRequestCount(int N0)
  {
    SimulationConfig config = debug ? new SimulationConfig("src/main/resources/config.json")
                                    : new SimulationConfig("config.json");
    double lastP = -1;
    while (true)
    {
      if (lastP == -1)
      {
        Simulator s0 = new Simulator(config.getSources(), config.getBuffer(), config.getProcessors(), N0);
        s0.startSimulation(false);

        lastP = (double) s0.getProductionManager().getFullRejectCount() / (double) N0;
        if (lastP == 0 || lastP == 1)
        {
          return -1;
        }
      }
      int N1 = (int) Math.round(Ta * (1 - lastP) / (lastP * d * d));

      Simulator s1 = new Simulator(config.getSources(), config.getBuffer(), config.getProcessors(), N1);
      s1.startSimulation(false);

      double p1 = (double) s1.getProductionManager().getFullRejectCount() / (double) N1;
      if (p1 == 0 || p1 == 1)
      {
        return -2;
      }

      System.out.println(
        "N0=" + N0 + " p0=" + lastP + " N1=" + N1 + " p1=" + p1 + "  [abs=" + Math.abs(lastP - p1) + ", dp0=" +
        (0.1 * lastP) + "]");
      if (Math.abs(lastP - p1) < 0.1 * lastP)
      {
        break;
      }
      N0 = N1;
      lastP = p1;
    }
    return N0;
  }
}
