package jobs;

public class NopJob extends AbstractJob {
	public NopJob() {
		super(null);
	}

	@Override
	public void run() {
	}
}
