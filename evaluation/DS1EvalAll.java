package at.grid.sepp3.core.evaluation;

import at.grid.cms.element.CmsElementSummary;
import at.grid.cms.search.CmsBasicSearch;
import at.grid.sepp3.core.app.SeppApplication;
import at.grid.sepp3.core.app.SeppProject;
import at.grid.sepp3.core.element.CmsHorizont;
import at.grid.sepp3.core.element.CmsProfil;
import at.grid.sepp3.core.element.CmsProject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.openide.util.Lookup;

/**
 * This class is used to load all profil elements and all corresponding horizont
 * elements in a separate thread.
 *
 * @author pk
 */
public class DS1EvalAll implements ProgressRunnable<String> {

  /**
   * SEPP application
   */
  SeppApplication app = Lookup.getDefault().lookup(SeppApplication.class);
//  private SeppProject app = Lookup.getDefault().lookup(SeppProject.class);
  /**
   * SEPP project CMS element
   */
  private CmsProject project = null;

  ArrayList<String> log = new ArrayList<String>();

  public DS1EvalAll() {
    // get CMS project
    if (app.isProjectOpen()) {
      this.project = app.getProject().getProjectElement();
    }
    this.log.clear();
  }

  @Override
  public String run(ProgressHandle handle) {

    StringBuilder sb = new StringBuilder();
    ArrayList<String> log;

    if (project == null) {
      return "Project not loaded";
    }

    // get all profiles
    CmsBasicSearch s = new CmsBasicSearch(app.getProject(), app.getProject().getUser(), "profil");
    s.addSelectMeta();
    ArrayList<CmsElementSummary> eles = s.search().getResultAsElementSummary();
    // prepare progress handler
    int total = eles.size();

    handle.switchToDeterminate(total * 10);

    int counter = 0;

    for (CmsElementSummary ele : eles) {

      int profilcounter = 1;
      handle.progress("Profil laden ...", counter + profilcounter);
      ++profilcounter;

      CmsProfil profil = (CmsProfil) ele.getElement();

      // get horizonte
      ArrayList<CmsHorizont> horizonte = new ArrayList<CmsHorizont>();
      List<CmsElementSummary> horizontEles = profil.getRelationAttribute("horizonte").getElements();
      for (CmsElementSummary horizontEle : horizontEles) {
        handle.progress("Horizont laden ...", counter + profilcounter);
        ++profilcounter;
        horizonte.add((CmsHorizont) horizontEle.getElement());
      }

      handle.progress("Profil '" + profil.getTitle() + "' -> Bewertung...", counter + 8);

      // loop through horizont and calculate complex parameters
      for (CmsHorizont horizont : horizonte) {
        DS1HorizontComplexCalc hCalc = new DS1HorizontComplexCalc(profil, horizont);
        hCalc.calcAll();
        log = hCalc.getLog();
        for (String l : log) {
          app.getProject().getLogger().log(Level.INFO, l);
        }
        this.log.addAll(log);
        horizont.save();
      }

      DS1ProfilComplexCalc calc = new DS1ProfilComplexCalc(profil);
      calc.calcAll();
      log = calc.getLog();
      for (String l : log) {
        app.getProject().getLogger().log(Level.INFO, l);
      }
      this.log.addAll(log);

      
      DS1ProfilEval eval = new DS1ProfilEval(project, profil);
      eval.evalAll();
      log = eval.getLog();
      for (String l : log) {
        app.getProject().getLogger().log(Level.INFO, l);
      }
      this.log.addAll(log);

      handle.progress("Profil '" + profil.getTitle() + "' speichern ...", counter + 9);
      profil.save();

      counter += 10;

    }

    handle.finish();

    return sb.toString();
  }

  public ArrayList<String> getLog() {
    return log;
  }

  public String getLogAsString() {
    StringBuilder sb = new StringBuilder();
    for (String s : this.log) {
      sb.append(s).append("\n");
    }
    return sb.toString();
  }
}
