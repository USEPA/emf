package gov.epa.emissions.framework.services.fast;

public class FastOutputExportWrapper {

    private FastAnalysis analysis;

    private FastRun run;

    private FastRunOutput runOutput;

    private FastAnalysisOutput analysisOutput;

    public FastOutputExportWrapper(FastAnalysis analysis, FastAnalysisOutput analysisOutput) {

        this.analysis = analysis;
        this.analysisOutput = analysisOutput;
    }

    public FastOutputExportWrapper(FastRun run, FastRunOutput runOutput) {

        this.run = run;
        this.runOutput = runOutput;
    }

    public FastAnalysis getAnalysis() {
        return analysis;
    }

    public FastRun getRun() {
        return run;
    }

    public FastRunOutput getRunOutput() {
        return runOutput;
    }

    public FastAnalysisOutput getAnalysisOutput() {
        return analysisOutput;
    }

    public boolean isRunOutput() {
        return this.run != null && this.runOutput != null;
    }

    public boolean isAnalysisOutput() {
        return this.analysis != null && this.analysisOutput != null;
    }

    public int getOutputDatasetId() {

        int id = 0;

        if (this.isAnalysisOutput()) {
            id = this.analysisOutput.getOutputDataset().getId();
        } else if (this.isRunOutput()) {
            id = this.runOutput.getOutputDataset().getId();
        }

        return id;
    }

    public int getOutputDatasetVersion() {

        int version = 0;

        if (this.isAnalysisOutput()) {
            version = this.analysisOutput.getOutputDataset().getDefaultVersion();
        } else if (this.isRunOutput()) {
            version = this.runOutput.getOutputDataset().getDefaultVersion();
        }

        return version;
    }

    public int getGridId() {

        int id = 0;

        if (this.isAnalysisOutput()) {
            id = this.analysis.getGrid().getId();
        } else if (this.isRunOutput()) {
            id = this.run.getGrid().getId();
        }

        return id;
    }

    public String getName() {

        String name = "";

        if (this.isAnalysisOutput()) {
            name = this.analysisOutput.getOutputDataset().getName();
        } else if (this.isRunOutput()) {
            name = this.runOutput.getOutputDataset().getName();
        }

        return name;
    }
}
