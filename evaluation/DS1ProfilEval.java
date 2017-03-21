package at.grid.sepp3.core.evaluation;

import at.grid.cms.attribute.KeyAttribute;
import at.grid.cms.element.CmsElementSummary;
import at.grid.sepp3.core.app.SeppLogCollector;
import at.grid.sepp3.core.element.CmsHorizont;
import at.grid.sepp3.core.element.CmsProfil;
import at.grid.sepp3.core.element.CmsProject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class calculates the complex parameters for a given CmsProfil element.
 *
 * @author pk
 */
public class DS1ProfilEval {

//##############################################################################
//  CLASS FIELDS
//##############################################################################
  /**
   * The profil used for calculation
   */
  private final CmsProfil profil;

  private final CmsProject project;
  /**
   * List of all horizonte
   */
  private ArrayList<CmsHorizont> horizonte;
  /**
   * List of Oberboden-Horizonte
   */
  private ArrayList<CmsHorizont> oberboden;
  /**
   * List of Unterboden-Horizonte
   */
  private ArrayList<CmsHorizont> unterboden;
  private Integer bewertungsBereich;
  /**
   * Calculation log
   */
//  ArrayList<String> log = new ArrayList<String>();
  SeppLogCollector log = new SeppLogCollector();

//##############################################################################
//  CONSTRUCTOR
//##############################################################################
  public DS1ProfilEval(CmsProject project, CmsProfil profil) {
    // set project
    this.project = project;
//    this.project = 
    this.profil = profil;
    // get list of all horizonte
    this.horizonte = new ArrayList<CmsHorizont>();
    List<CmsElementSummary> eles = this.profil.getRelationAttribute("horizonte").getElements();
    for (CmsElementSummary ele : eles) {
      CmsHorizont h = (CmsHorizont) ele.getElement();
      this.horizonte.add(h);
    }
    initialize();
  }

  private void initialize() {

    // define oberboden
    log.add("-> Oberboden wird definiert");
    this.oberboden = new ArrayList<CmsHorizont>();
    long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();

    if (bodentyp == 2100 || bodentyp == 2110 || bodentyp == 2111
            || bodentyp == 2112 || bodentyp == 2120) {
      log.add("   Moorboden! Es werden H* oder T* Horizonte gesucht");
      // Moore (Oberboden H1, H2, Hn bzw. T1, Tn)
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValue("bezeichnung").toUpperCase().startsWith("H")
                || h.getAttributeValue("bezeichnung").toUpperCase().startsWith("T")) {
          this.oberboden.add(h);
        }
      }  //M#Horizontabfrage wird weggelassen da in der öst. Systematik M der Wurzelfilz ist und wir Arig für umgelagerten A-Horzont verwenden
//    } else if (bodentyp >= 1700 && bodentyp <= 1799) {
//      log.add("   Schüttungsboden! Es werden M* Horizonte gesucht");
//      // Alle 'M' Horizonte finden und der Liste hinzufügen
//      for (CmsHorizont h : this.horizonte) {
//        if (h.getAttributeValue("bezeichnung").toUpperCase().startsWith("M")) {
//          this.oberboden.add(h);
//        }
//      }
    } else {
      log.add("   Terrestrischer Boden! Es werden A* Horizonte gesucht");
      // Alle 'A' Horizonte finden und der Liste hinzufügen
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValue("bezeichnung").toUpperCase().startsWith("A")) {
          this.oberboden.add(h);
        }
      }
    }

    if (this.oberboden.isEmpty()) {
      log.add("   Keine der Bedingungen erfüllt! Es wird der höchstgelegene Horizont ausgewählt");
      for (CmsHorizont h : this.horizonte) {
        if (h.getUpperLimit() == 0) {
          this.oberboden.add(h);
        }
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append("   Oberboden -> ");
    for (CmsHorizont h : this.oberboden) {
      sb.append(h.getAttributeValue("bezeichnung")).append(" ");
    }
    log.add(sb.toString());

    // define unterboden
    log.add("-> Unterboden wird definiert");
    this.unterboden = new ArrayList<CmsHorizont>();

    long bodentypCode = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");

    if (bodentypCode == 2100 || bodentypCode == 2110 || bodentypCode == 2111
            || bodentypCode == 2112 || bodentypCode == 2120) {
      log.add("   Moorboden! Es werden Horizonte gesucht, die keine H* oder T* Horizonte sind, deren Untergrenze sich aber noch im Wurzelraum befindet (Tiefe <= Wurzeltiefe)");

      for (CmsHorizont h : this.horizonte) {
        if (!(h.getAttributeValue("bezeichnung").toUpperCase().startsWith("H")
                || h.getAttributeValue("bezeichnung").startsWith("T"))
                && (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= wurzelTiefe)) {
          this.unterboden.add(h);
        }
      }
    //} else if (bodentypCode >= 1700 && bodentypCode <= 1799) { //siehe oben bei Oberbeodendefinition
    //  log.add("   Schüttungsboden! Es werden Horizonte gesucht, die keine M* Horizonte sind, deren Untergrenze sich aber noch im Wurzelraum befindet (Tiefe <= Wurzeltiefe)");
    //  for (CmsHorizont h : this.horizonte) {
    //    if (!h.getAttributeValue("bezeichnung").toUpperCase().startsWith("M")
    //            && (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= wurzelTiefe)) {
    //      this.unterboden.add(h);
    //    }
    //  }
    } else {
      log.add("   Terrestricher Boden! Es werden Horizonte gesucht, die keine M* Horizonte sind, deren Untergrenze sich aber noch im Wurzelraum befindet (Tiefe <= Wurzeltiefe)");
      for (CmsHorizont h : this.horizonte) {
        if (!h.getAttributeValue("bezeichnung").toUpperCase().startsWith("A")
                && (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= wurzelTiefe)) {
          this.unterboden.add(h);
        }
      }
    }

    StringBuilder sb2 = new StringBuilder();
    sb2.append("   Unterboden -> ");
    for (CmsHorizont h : this.unterboden) {
      sb2.append(h.getAttributeValue("bezeichnung")).append(" ");
    }
    if (this.unterboden.isEmpty()) {
      sb2.append("Kein Horizont ist dem Unterboden zugeteilt!");
    }
    log.add(sb2.toString());

  }

//##############################################################################
//  HELPER METHODS
//##############################################################################
  public boolean isAttributeValueSet(String att) {
    String value = this.profil.getAttributeValue(att);
    if (value != null && !"".equalsIgnoreCase(value)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Very basic check to assert non-empty float attributes (assuming negative
   * value corresponds to NoData)
   *
   * @param att
   * @return
   */
  public boolean checkFloatAttribute(String att) {
    float value = this.profil.getAttributeValueFloat(att);
    if (value >= 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the largest of the horizons returned by defineOberboden()
   *
   * @return
   */
  public CmsHorizont selectLargestOberbodenHrz() {
    CmsHorizont oberbodenHrz = null;
    if (!this.oberboden.isEmpty()) {
      int max = 0;
      for (CmsHorizont h : this.oberboden) {
        int thickness = h.getThickness();
        if (thickness > max) {
          oberbodenHrz = h;
          max = thickness;
        }
      }
    }
    if (oberbodenHrz != null) {
      log.add("   Mächtigster Horizont im Oberboden -> " + oberbodenHrz.getAttributeValue("bezeichnung"));
    }
    // falls kein Horizont gefunden wurde, return 'null'
    return oberbodenHrz;
  }

  /**
   * Returns the largest of the horizons returned by defineUnterboden()
   *
   * @return
   */
  public CmsHorizont selectLargestUnterbodenHrz() {
    CmsHorizont unterbodenHrz = null;
    if (!this.unterboden.isEmpty()) {
      int max = 0;
      for (CmsHorizont h : this.unterboden) {
        int thickness = h.getThickness();
        if (thickness > max) {
          unterbodenHrz = h;
          max = thickness;
        }
      }
    }
    if (unterbodenHrz != null) {
      log.add("   Mächtigster Horizont im Oberboden -> " + unterbodenHrz.getAttributeValue("bezeichnung"));
    }
    // falls kein Horizont gefunden wurde, return 'null'
    return unterbodenHrz;
  }

  /**
   * Berechnen des zu bewertenden Bereich (BB).
   *
   * @return Untergrenze
   */
  public int getBB() {

    if (this.bewertungsBereich == null) {
      log.add("   Bestimmung des zu bewertenden Bereiches (BB)");
      Integer bb = null;
      long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();
      Float flurabstand = this.profil.getAttributeValueFloat("flurabstand");

      if ((flurabstand > 0 && flurabstand <= 100)
              || (bodentyp == 1820 || bodentyp == 1821 || bodentyp == 1822 // Gleye
              || (bodentyp >= 2000 && bodentyp <= 2033))) {
        log.add("   Grundwasser -> Flurabstand wurde gesetzt oder Boden ist dem Gleye zuzuteilen");
        log.add("   Es wird geprüft, ob ein 'Gr' oder 'G2' Horizont vorhanden ist");
        for (CmsHorizont h : this.horizonte) {
          String bezeichnung = h.getAttributeValue("bezeichnung");
          if ("Gr".equalsIgnoreCase(bezeichnung) || "G2".equalsIgnoreCase(bezeichnung)) {
            int limit = h.getUpperLimit();
            if (limit >= 0) {
              log.add("   " + bezeichnung + "-Horizont gefunden! Obergrenze dieses Horizont (" + limit + " cm) wird als Untergrenze des BBs definiert");
              bb = limit;
            }
          }
        }
      } else if (bodentyp >= 1900 && bodentyp <= 1950) {
        log.add("   Stauwasser -> Es wird geprüft, ob ein 'Sd' oder 'S-' Horizont vorhanden ist");
        for (CmsHorizont h : this.horizonte) {
          String bezeichnung = h.getAttributeValue("bezeichnung");
          if (bezeichnung.startsWith("S")) { // Sd, S- nicht differenziert
            int limit = h.getUpperLimit();
            if (limit >= 0) {
              log.add("   " + bezeichnung + "-Horizont gefunden! Obergrenze dieses Horizont (" + limit + " cm) wird als Untergrenze des BBs definiert");
              bb = limit;
            }
          }
        }
      } else if (bodentyp >= 2100 && bodentyp <= 2120) {
        log.add("   Moorboden -> Es wird zuerst geprüft, ob der Flurabstand gesetzt wurde");
        // 3) Moore
        if (flurabstand >= 0 && flurabstand <= 100) {
          log.add("   GW-Flurabstand bekannt, diese Tiefe wird als die Untergrenze des BBs definiert");
          // Grundwasserstand bekannt ... in Zentimeter umrechnen
          bb = flurabstand.intValue() * 100;
        } else {
          log.add("   GW-Flurabstand nicht gesetzt. Überprüfen, ob ein Gr-Horizont vorhanden ist");
          for (CmsHorizont h : this.horizonte) {
            if (h.getAttributeValue("bezeichnung").startsWith("Gr")) {
              int limit = h.getUpperLimit();
              if (limit >= 0) {
                log.add("   Gr-Horizont gefunden! Obergrenze dieses Horizont (" + limit + " cm) wird als Untergrenze des BBs definiert");
                bb = limit;
              }
            }
          }
        }
      } else {
        // 4) Horizont mit kf-Wert 1 oder 0
        log.add("   Gibt es Horizont(e) mit einem kf-Wert von 0 oder 1? Wenn ja, den obersten als Stauhorizont definieren");
        ArrayList<CmsHorizont> kfList = new ArrayList<CmsHorizont>();
        for (CmsHorizont h : this.horizonte) {
          if (h.getAttributeValueFloat("Px12_kf") == 0
                  || h.getAttributeValueFloat("Px12_kf") == 1) {
            kfList.add(h);
          }
        }

        log.add("   " + kfList.size() + " Horizont(e) mit einem kf-Wert von 0 oder 1 gefunden");

        // Obersten Horizont (mit minimaler Tiefe)
        int min = 100;
        if (!kfList.isEmpty()) {
          CmsHorizont topH = null;
          for (CmsHorizont h : kfList) {
            int depth = h.getAttributeValueInt("tiefe");
            if (depth >= 0 && depth <= min) {
              bb = h.getUpperLimit();
              min = depth;
              topH = h;
            }
          }
          if (topH == null) {
            log.add("   BB-Untergrenze nicht gesetzt, kein Horizont definiert");
          } else {
            log.add("   BB-Untergrenze auf Obergrenze von " + topH.getAttributeValue("bezeichnung") + "-Horizont gesetzt -> " + bb + " cm");
          }
        } else {
          log.add("   BB-Untergrenze nicht gesetzt");
        }
      }

      if (bb == null) {
        log.add("   BB-Untergrenze wird auf Standardtiefe gesetzt -> 100 cm");
        bb = 100;
      }

      // BB auf ungültigen Wert überprüfen
      if (bb <= 0 || bb > 100) {
        log.add("   Ungültiger Wert für BB-Untergrenze, wird auf Standardtiefe gesetzt -> 100 cm");
        bb = 100;
      }

      this.bewertungsBereich = bb;
    }
    return this.bewertungsBereich;
  }

  /**
   * Selects the most precise Bodenart value found for a specific horizont
   *
   * @param h
   * @return
   */
  public long defineBodenart(CmsHorizont h) {
    long bodenartCode = ((KeyAttribute) h.getAttribute("bodenart")).getId();
    long bodenartlaborCode = ((KeyAttribute) h.getAttribute("bodenartlabor")).getId();
    long bodenart = -1;

    if (bodenartlaborCode >= 0) {
      log.add("   " + h.getAttributeLabel("bodenartlabor") + "  gefunden");
      log.add("   -> " + ((KeyAttribute) h.getAttribute("bodenartlabor")).getValue());
      bodenart = bodenartlaborCode;
    } else if (bodenartCode >= 0) {
      log.add("   " + h.getAttributeLabel("bodenart") + "  gefunden");
      log.add("   -> " + ((KeyAttribute) h.getAttribute("bodenart")).getValue());
      bodenart = bodenartCode;
    }
    return bodenart;
  }

//##############################################################################
//  CALCULATION METHODS
//##############################################################################
  /**
   * Perform all calculation and write to output
   */
  public void evalAll() {

    log.add("Profil '" + this.profil.getTitle() + "' -> Bewertung gestartet!");

    this.calculate1A2Dry();
    this.calculate1A2Humid();
    this.calculate1A3();
    this.calculate1A4();
    this.calculate1C1ave();
    this.calculate1C1min();
    this.calculate1C2();
    this.calculate1C3();
    this.calculate1C4();
    this.calculate1C5();
    this.calculate1D1();
    this.calculate1D2();
    this.calculate1D3();
    this.calculate1D4();
    this.calculate1D5();
    this.calculate2A();
    this.calculate2B();

    log.add("Profil '" + this.profil.getTitle() + "' -> Bewertung fertig!");
  }

//##############################################################################
//  SOIL EVALUATION METHODS
//##############################################################################
  public void calculate1A2Dry() {
    //  output
    log.add("-------------------");
    log.add("1) " + this.profil.getAttributeLabel("Leben_Tr"));
    Integer result = null;

    // get attributes
    Long[] landnutzung = this.profil.getAttributeMultikeyValueIds("landnutzung");
    List<Long> list = null;
    if (landnutzung != null) {
      list = new ArrayList<Long>(Arrays.asList(landnutzung));
    }
    long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();
    float sx04a = this.profil.getAttributeValueFloat("Sx04a_nFKwe");

    log.add("   Überprüfe Landnutzung und Bodentyp");
    if (list.contains(870L)) {
      log.add("   Ruderalstandort!");
      result = 1;
    } else if (list.contains(950L)) {
      log.add("   Moor, unkultiviert!");
      result = 2;
    } else if (bodentyp >= 0 && (bodentyp == 1720 || bodentyp == 1721 || bodentyp == 1722)) {
      log.add("   Kulturrohboden!");
      result = 1;
    } else if (bodentyp >= 0
            && (bodentyp == 1900 || bodentyp == 1910 || bodentyp == 1920 || bodentyp == 1921
            || bodentyp == 1922 || bodentyp == 1930 || bodentyp == 1940 || bodentyp == 1950
            || bodentyp == 2000 || bodentyp == 2010 || bodentyp == 2011 || bodentyp == 2012
            || bodentyp == 2020 || bodentyp == 2021 || bodentyp == 2022 || bodentyp == 2023
            || bodentyp == 2030 || bodentyp == 2031 || bodentyp == 2032 || bodentyp == 2033)) {
      log.add("   Gley oder Pseudogley!");
      result = 4;
    } else if (bodentyp >= 0 && (bodentyp == 2100 || bodentyp == 2110 || bodentyp == 2111 || bodentyp == 2112 || bodentyp == 2120)) {
      log.add("   Moor!");
      result = 5;
    }
    log.add("   Profil konnte nicht anhand Landnutzung oder Bodentyp bewertet werden");
    log.add("   Es wird versucht, über diesen Boden anhand der nFK zu bewerten");

    if (result == null) {
      if (sx04a < 0) {
        log.add("   FEHLER => nFK für den eff. Wurzelraum nicht vorhanden! Es wird mindestens dieser Parameter benötigt");
        return;
      } else if (sx04a <= 30) {
        result = 1;
      } else if (sx04a > 30 && sx04a <= 60) {
        result = 2;
      } else if (sx04a > 60 && sx04a <= 90) {
        result = 3;
      } else if (sx04a > 90 && sx04a <= 220) {
        result = 4;
      } else if (sx04a > 220) {
        result = 5;
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Leben_Tr", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1A2Humid() {
    //  output
    log.add("-------------------");
    log.add("2) " + this.profil.getAttributeLabel("Leben_Fe"));
    Integer result = null;

    // get atts
    float flurabstand = this.profil.getAttributeValueFloat("flurabstand");
    long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();
    float sx04a = this.profil.getAttributeValueFloat("Sx04a_nFKwe");

    log.add("   Überprüfe Bodentyp");
    if (bodentyp >= 0 && (bodentyp == 2100 || bodentyp == 2110 || bodentyp == 2111 || bodentyp == 2112 || bodentyp == 2120)) {
      log.add("   Moor!");
      result = 1;
    } else if (bodentyp >= 0
            && (bodentyp == 1900 || bodentyp == 1910 || bodentyp == 1920 || bodentyp == 1921
            || bodentyp == 1922 || bodentyp == 1930 || bodentyp == 1940 || bodentyp == 1950
            || bodentyp == 2000 || bodentyp == 2010 || bodentyp == 2011 || bodentyp == 2012
            || bodentyp == 2020 || bodentyp == 2021 || bodentyp == 2022 || bodentyp == 2023
            || bodentyp == 2030 || bodentyp == 2031 || bodentyp == 2032 || bodentyp == 2033)) {
      log.add("   Gley oder Pseudogley!");
      result = 2;
    } else if (bodentyp >= 0
            && (bodentyp == 1800 || bodentyp == 1810 || bodentyp == 1811 || bodentyp == 1812
            || bodentyp == 1820 || bodentyp == 1821 || bodentyp == 1822 || bodentyp == 1830
            || bodentyp == 1831 || bodentyp == 1832 || bodentyp == 1840 || bodentyp == 1841
            || bodentyp == 1842)) {
      log.add("   Au- und Schwemmboden!");
      if (sx04a >= 0 && sx04a > 220) {
        log.add("   Ergebnis wird aufgewertet, da nFK für den eff. Wurzelraum > 220 ist");
        result = 2;
      } else if (sx04a >= 0 && sx04a < 220) {
        log.add("   nFK für den eff. Wurzelraum zu klein, Ergebnis wird nicht aufgewertet");
        result = 3;
      } else if (sx04a >= 0 && sx04a > 220) { //Ich verstehe diese wiederholte Abfrage nicht
        log.add("   nFK für den eff. Wurzelraum entweder nicht vorhanden, Ergebnis wird nicht aufgewertet");
        log.add("   Hinweis: mit dem komplexen Parameter '" + this.profil.getAttributeLabel("Sx04a_nFKwe") + "' wird die Bewertung genauer");
        result = 3;
      }
    } else if (flurabstand >= 0 && flurabstand < 0.2) {
      log.add("   Bewertung nach GW-Flurabstand!");
      result = 1;
    } else if (flurabstand >= 0 && flurabstand >= 0.2 && flurabstand < 0.5) {
      log.add("   Bewertung nach GW-Flurabstand!");
      result = 2;
    } else if (flurabstand >= 0 && flurabstand >= 0.5 && flurabstand < 1) {
      log.add("   Bewertung nach GW-Flurabstand!");
      result = 3;
    }
    if (result == null) {
      if (sx04a < 0) {
        log.add("   FEHLER => nFK für den eff. Wurzelraum nicht vorhanden! Es wird mindestens dieser Parameter benötigt");
        return;
      } else if (sx04a > 220) {
        result = 2;
      } else if (sx04a > 140 && sx04a <= 220) {
        result = 3;
      } else if (sx04a > 60 && sx04a <= 140) {
        result = 4;
      } else if (sx04a <= 60) {
        result = 5;
      }
    }
    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Leben_Fe", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1A3() {
    //  output
    log.add("-------------------");
    log.add("3) " + this.profil.getAttributeLabel("Leben_Org"));
    Integer result = null;

    CmsHorizont h = this.selectLargestOberbodenHrz();

    // Es konnte kein Referenz-Horizont gefunden werden
    if (h == null) {
      log.add("   FEHLER => Es konnte kein Referenz-Horizont gefunden werden! (mächtigster Horizont im Oberboden)");
      return;
    }

    float ph = h.getAttributeValueFloat("ph_wert");
    if (ph < 0) {
      log.add("   FEHLER => pH-Wert für Referenz-Horizont nicht vorhanden!");
      return;
    } else // pH-Wert wurde gefunden, Berechnung
    if (ph >= 4.2) {
      log.add("   BLGT A -> pH-Wert >= 4.2");
      // BLGT A

      // Parameter
      // Feuchtestufe (S161)
      long feuchte = ((KeyAttribute) this.profil.getAttribute("oekofeuchte")).getId();
      if (feuchte < 0) {
        log.add("   FEHLER => Feuchtegrad für Referenz-Horizont nicht vorhanden!");
        return;
      }

      // Landnutzung
      Long[] landnutzung = this.profil.getAttributeMultikeyValueIds("landnutzung");
      List<Long> list = null;
      if (landnutzung != null) {
        list = new ArrayList<Long>(Arrays.asList(landnutzung));
      }

      // Bodenart
      long bodenart = this.defineBodenart(h);

      // Bodentyp
      long bodentypCode = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();

      // Berechnung
      if (feuchte == 220 || feuchte == 230 || feuchte == 240
              || feuchte == 250 || feuchte == 260 || feuchte == 280) { // inkl. 'wechselfeucht'
        log.add("   -> A1");
        if (list.contains(100L)) {
          log.add("   -> A1.1");
          result = 3;
        } else if (list.contains(200L) || list.contains(2990L) || list.contains(500L)) { //Hier noch Sonderkultur/Weinbau integriert da sonst keine Bewertung stattfindet. 
          if (feuchte == 260) {
            log.add("   -> A1.3");
            result = 3;
          } else {
            log.add("   Grünland oder Sonderkultur!"); //Siehe oben
            if (bodenart < 0) {
              log.add("   FEHLER => Bodenart für Referenz-Horizont nicht vorhanden!");
              return;
            }
            if (bodenart == 101 || bodenart == 121 || bodenart == 231 || bodenart == 341) {
              log.add("   -> A1.2.1");
              result = 2;
            } else if (bodenart == 212 || bodenart == 202 || bodenart == 332
                    || bodenart == 312 || bodenart == 423 || bodenart == 403) {
              log.add("   -> A1.4.2");
              result = 1;
            } else if (bodenart == 504 || bodenart == 534
                    || (bodentypCode >= 0 && (bodentypCode == 2100 || bodentypCode == 2110
                    || bodentypCode == 2111 || bodentypCode == 2112 || bodentypCode == 2120))) {
              log.add("   -> A1.2.3");
              result = 2;
            }
          }
        } else if (list.contains(300L)) {
          log.add("   Acker!");

          if (bodenart < 0) {
            log.add("   FEHLER => Bodenart für Referenz-Horizont nicht vorhanden!");
            return;
          }
          if (bodenart == 101) {
            log.add("   -> A1.4.1");
            result = 4;
          } else if (bodenart == 121 || bodenart == 231 || bodenart == 341
                  || bodenart == 212 || bodenart == 202 || bodenart == 332
                  || bodenart == 313 || bodenart == 403 || bodenart == 423) {
            log.add("   -> A1.4.2");
            result = 2;
          } else if (bodenart == 504 || bodenart == 534
                  || (bodentypCode >= 0 && (bodentypCode == 2100 || bodentypCode == 2110
                  || bodentypCode == 2111 || bodentypCode == 2112 || bodentypCode == 2120))) {
            log.add("   -> A1.4.3");
            result = 3;
          }
        }

      } else if (feuchte == 270) {
        log.add("   -> A2");
        if (ph >= 5.5) {
          log.add("   -> A2.1");
          result = 4;
        } else if (ph < 5.5) {
          log.add("   -> A2.2");
          result = 4;
        }
      } else if (feuchte == 210) {
        log.add("   -> A3");
        result = 4;
      }

    } else if (ph < 4.2) {
      log.add("   BLGT B -> pH-Wert < 4.2");
      // BLGT B
      // Feuchtestufe (S161)
      long feuchte = ((KeyAttribute) this.profil.getAttribute("oekofeuchte")).getId();
      if (feuchte < 0) {
        log.add("   FEHLER => Feuchtegrad für Referenz-Horizont nicht vorhanden!");
        return;
      }

      if (feuchte == 220 || feuchte == 230 || feuchte == 240
              || feuchte == 250 || feuchte == 260 || feuchte == 280) {
        log.add("   -> B1");
        result = 4;
      } else if (feuchte == 270) {
        log.add("   -> B2");
        result = 5;
      } else if (feuchte == 210) {
        log.add("   -> B3");
        result = 5;
      }
    }

    if (result == null) {
      log.add("   FEHLER => Konnte nicht bewertet werden, fällt durch die Entscheidungsmatrix!");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Leben_Org", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1A4() {
    //  output
    log.add("-------------------");
    log.add("4) " + this.profil.getAttributeLabel("Leben_Kult"));
    Integer result = null;
    // Dem Benutzer wird hier nicht bekannt, wenn einzelne Kriterien nicht
    // berechnet werden koennen, sie fliessen einfach nicht in die Endbewertung ein.
    ArrayList<Integer> resultList = new ArrayList<Integer>();

    // A) Allgemeine Standortsbedingungen
    log.add("   A) Allgemeine Standortsbedingungen");

    log.add("   A1");
    float sx12 = this.profil.getAttributeValueFloat("Sx12_We");
    if (sx12 < 0) {
      log.add("   ACHTUNG => Komplexer Parameter fehlt! -> siehe " + this.profil.getAttributeLabel("Sx12_We"));
      log.add("   HINWEIS => A1 wird nur bewertet, wenn dieser Parameter vorhanden ist!");
    }

    if (sx12 >= 100) {
      resultList.add(1);
      log.add("   A1 -> 1");
    } else if (sx12 >= 80 && sx12 < 100) {
      resultList.add(2);
      log.add("   A1 -> 2");
    } else if (sx12 >= 60 && sx12 < 80) {
      resultList.add(3);
      log.add("   A1 -> 3");
    } else if (sx12 >= 40 && sx12 < 60) {
      resultList.add(4);
      log.add("   A1 -> 4");
    } else if (sx12 >= 0 && sx12 < 40) {
      resultList.add(5);
      log.add("   A1 -> 5");
    }

    log.add("   A2");
    boolean a2 = true;
    log.add("   Zur Bestimmung der Struktur des Oberbodens wird der mächtigste Horizont ermittelt");

    CmsHorizont oStrukturHrz = this.selectLargestOberbodenHrz();
    if (oStrukturHrz == null) {
      log.add("   ACHTUNG => Mächtigster Horizont im Oberboden konnte nicht ermittelt werden!");
      log.add("   HINWEIS => A2 wird nur bewertet, wenn dies möglich ist!");
      a2 = false;
    }

    log.add("   Im Ober- und Unterboden wird jeweils der Horizont gesucht, der die höchste Lagerungsdichte aufweist");

    CmsHorizont oDichteHrz = null;
    float oMaxDichte = 0;
    for (CmsHorizont h : this.oberboden) {
      float px03a = h.getAttributeValueFloat("Px03a_LdZ");
      if (px03a < 0) {
        log.add("   ACHTUNG => Komplexer Parameter nicht für alle Horizonte im Oberboden vorhanden! -> " + h.getAttributeLabel("Px03a_LdZ"));
        log.add("   HINWEIS => A2 wird nur bewertet, wenn dieser Parameter für alle Oberboden-Horizonte vorhanden ist!");
        a2 = false;
      }
      if (px03a > oMaxDichte) {
        oMaxDichte = px03a;
        oDichteHrz = h;
      }
    }

    if (oDichteHrz == null) {
      log.add("   ACHTUNG => Horizont mit der höchsten Lagerungsdichte im Oberboden konnte nicht festgestellt werden");
      log.add("   HINWEIS => A2 wird nur bewertet, wenn dies möglich ist!");
      a2 = false;
    }

    CmsHorizont uDichteHrz = null;
    float uMaxDichte = 0;
    for (CmsHorizont h : this.unterboden) { //hier habe ich oberboden durch unterboden ersetzt
      float px03a = h.getAttributeValueFloat("Px03a_LdZ");
      if (px03a < 0) {
        log.add("   ACHTUNG => Komplexer Parameter nicht für alle Horizonte im Unterboden vorhanden! -> " + h.getAttributeLabel("Px03a_LdZ"));
        log.add("   HINWEIS => A2 wird nur bewertet, wenn dieser Parameter für alle Unterboden-Horizonte vorhanden ist!");
        a2 = false;
      }
      if (px03a > uMaxDichte) {
        uMaxDichte = px03a;
        uDichteHrz = h;
      }
    }

    if (uDichteHrz == null) {
      log.add("   ACHTUNG => Horizont mit der höchsten Lagerungsdichte im Unterboden konnte nicht festgestellt werden");
      log.add("   HINWEIS => A2 wird nur bewertet, wenn dies möglich ist!");
      a2 = false;
    }

    if (a2) {
      if (((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 470
              && oStrukturHrz.getAttributeValueInt("gefuege1_anteil") >= 50
              && oDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4
              && uDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4) {
        log.add("   A2 -> 1");
        resultList.add(1);
      } else if (((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 470
              && (oStrukturHrz.getAttributeValueInt("gefuege1_anteil") >= 25
              && oStrukturHrz.getAttributeValueInt("gefuege1_anteil") < 50)
              && oDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4
              && uDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4) {
        log.add("   A2 -> 2");
        resultList.add(2);
      } else if ((((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 450
              || ((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 299
              || ((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 480)
              && oStrukturHrz.getAttributeValueInt("gefuege1_anteil") >= 50
              && oDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4
              && uDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4) {
        log.add("   A2 -> 3");
        resultList.add(3);
      } else if ((((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 450
              || ((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 299
              || ((KeyAttribute) oStrukturHrz.getAttribute("gefuege1")).getId() == 480)
              && oStrukturHrz.getAttributeValueInt("gefuege1_anteil") >= 50
              && oDichteHrz.getAttributeValueFloat("Px03a_LdZ") < 1.4
              && uDichteHrz.getAttributeValueFloat("Px03a_LdZ") >= 1.4
              && uDichteHrz.getAttributeValueFloat("Px03a_LdZ") <= 1.7) {
        log.add("   A2 -> 4");
        resultList.add(4);
      } else if (oDichteHrz.getAttributeValueFloat("Px03a_LdZ") >= 1.4
              || uDichteHrz.getAttributeValueFloat("Px03a_LdZ") > 1.7) {
        log.add("   A2 -> 5");
        resultList.add(5);
      } else {
        log.add("   A2 -> Konnte nicht bewertet werden! Profil fällt in keiner Kategorie hinein");
      }
    }

    log.add("   B) Wasserverhältnisse");

    log.add("   B1");
    float sx04a = this.profil.getAttributeValueFloat("Sx04a_nFKwe");
    if (sx04a < 0) {
      log.add("   ACHTUNG => Komplexer Parameter fehlt! -> siehe " + this.profil.getAttributeLabel("Sx04a_nFKwe"));
      log.add("   HINWEIS => B1 wird nur bewertet, wenn dieser Parameter vorhanden ist!");
    } else if (sx04a > 220) {
      log.add("   B1 -> 1");
      resultList.add(1);
    } else if (sx04a <= 220 && sx04a > 140) {
      log.add("   B1 -> 2");
      resultList.add(2);
    } else if (sx04a <= 140 && sx04a > 90) {
      log.add("   B1 -> 3");
      resultList.add(3);
    } else if (sx04a <= 90 && sx04a > 60) {
      log.add("   B1 -> 4");
      resultList.add(4);
    } else if (sx04a <= 60) {
      log.add("   B1 -> 5");
      resultList.add(5);
    }

    float flurabstand = this.profil.getAttributeValueFloat("flurabstand");
    long hangneigung = ((KeyAttribute) this.profil.getAttribute("hangneigung")).getId();
    if (hangneigung == 11 || hangneigung == 12 ){
        log.add("   B2");
        log.add("   HINWEIS => B2 wird nur bewertet weil Hangneigung 'eben' bzw 'schwach geneigt'! ");
        if (flurabstand < 0) {
          log.add("   ACHTUNG => Parameter fehlt! -> " + this.profil.getAttributeLabel("flurabstand"));
          log.add("   HINWEIS => B2 wird nur bewertet, wenn dieser Parameter vorhanden ist!");
        }

        if (flurabstand > 3) {
          log.add("   B2 -> 3");
          resultList.add(3);
        } else if (flurabstand <= 3 && flurabstand > 2) {
          log.add("   B2 -> 2");
          resultList.add(2);
        }else if (flurabstand <= 2 && flurabstand > 1) {
          log.add("   B2 -> 1");
          resultList.add(1);
        } else if (flurabstand <= 1 && flurabstand > 0.5) {
          log.add("   B2 -> 3");
          resultList.add(3);
        } else if (flurabstand <= 0.5) {
          log.add("   B2 -> 5");
          resultList.add(5);
        } 
    }

    log.add("   C) Durchlüftung");
    float sx07 = this.profil.getAttributeValueFloat("Sx06a_LKwe"); //hier war vorher LKOBEN, in der Tabelle in Anleitung wird aber LKwe verwendet, variablenname wurde aber beibehalten
    if (sx07 < 0) {
      log.add("   ACHTUNG => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx07_LKoben"));
      log.add("   HINWEIS => C wird nur bewertet, wenn dieser Parameter vorhanden ist!");
    }
    if (sx07 > 120) {
      log.add("   C -> 1");
      resultList.add(1);
    } else if (sx07 <= 120 && sx07 > 100) {
      log.add("   C -> 2");
      resultList.add(2);
    } else if (sx07 <= 100 && sx07 > 70) {
      log.add("   C -> 3");
      resultList.add(3);
    } else if (sx07 <= 70 && sx07 > 40) {
      log.add("   C -> 4");
      resultList.add(4);
    } else if (sx07 <= 40 && sx07 >= 0) {
      log.add("   C -> 5");
      resultList.add(5);
    }

    log.add("   D) Nährstoffversorgung");
    float sx15 = this.profil.getAttributeValueFloat("Sx15_MbWe");
    if (sx15 < 0) {
      log.add("   ACHTUNG => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx15_MbWe"));
      log.add("   HINWEIS => D wird nur bewertet, wenn dieser Parameter vorhanden ist!");
    }
    if (sx15 > 600) {
      log.add("   D -> 1");
      resultList.add(1);
    } else if (sx15 <= 600 && sx15 > 450) {
      log.add("   D -> 2");
      resultList.add(2);
    } else if (sx15 <= 450 && sx15 > 300) {
      log.add("   D -> 3");
      resultList.add(3);
    } else if (sx15 <= 300 && sx15 > 150) {
      log.add("   D -> 4");
      resultList.add(4);
    } else if (sx15 >= 0 && sx15 <= 150) {
      log.add("   D -> 5");
      resultList.add(5);
    }

    log.add("   E) Einfluss der Temperatur (Jahresmitteltemperatur)");
    boolean e = false; // was E already calculated

    float vegetation = project.getAttributeValueFloat("temp_veg");
    if (vegetation == -9999) {
      log.add("   ACHTUNG => Mitteltemperatur der Vegetationsperiode nicht gesetzt");
      log.add("   HINWEIS => E wird genauer bewertet, wenn dieser Parameter vorhanden ist!");
    } else {
      if (vegetation >= 18) {
        log.add("   E -> 1");
        resultList.add(1);
      } else if (vegetation >= 15 && vegetation < 18) {
        log.add("   E -> 2");
        resultList.add(2);
      } else if (vegetation >= 12 && vegetation < 15) {
        log.add("   E -> 3");
        resultList.add(3);
      } else if (vegetation >= 9 && vegetation < 12) {
        log.add("   E -> 4");
        resultList.add(4);
      } else if (vegetation < 9) {
        log.add("   E -> 5");
        resultList.add(5);
      }
      e = true;
    }

    if (!e) {
      float temp = project.getAttributeValueFloat("temperature");
      if (temp == -9999) {
        log.add("   ACHTUNG => Mittlere Jahrestemperatur nicht gesetzt");
        log.add("   HINWEIS => E wird genauer bewertet, wenn dieser Parameter vorhanden ist!");
      } else {
        if (temp >= 9.5) {
          log.add("   E -> 1");
          resultList.add(1);
        } else if (temp >= 8 && temp < 9.5) {
          log.add("   E -> 2");
          resultList.add(2);
        } else if (temp >= 6.5 && temp < 8) {
          log.add("   E -> 3");
          resultList.add(3);
        } else if (temp >= 5 && temp < 6.5) {
          log.add("   E -> 4");
          resultList.add(4);
        } else if (temp < 5) {
          log.add("   E -> 5");
          resultList.add(5);
        }
        e = true;
      }
    }
    if (!e) {
      long s181 = ((KeyAttribute) this.profil.getAttribute("hoehenstufe")).getId();
      if (s181 == -9999) {
        log.add("   ACHTUNG => Höhenstufe nicht gesetzt");
        log.add("   HINWEIS => E wird genauer bewertet, wenn dieser Parameter vorhanden ist!");
      }

      if (s181 >= 0) {
        if (s181 == 10 || s181 == 12) {
          log.add("   E -> 1");
          resultList.add(1);
        } else if (s181 == 20 || s181 == 21) {
          log.add("   E -> 2");
          resultList.add(2);
        } else if (s181 == 22) {
          log.add("   E -> 3");
          resultList.add(3);
        } else if (s181 == 23 || s181 == 31) {
          log.add("   E -> 4");
          resultList.add(4);
        } else if (s181 == 30 || s181 == 32 || s181 == 33) {
          log.add("   E -> 5");
          resultList.add(5);
        }
      }
    }

    // Zaehlen der Bewertungen pro Klasse
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int count4 = 0;
    int count5 = 0;

    for (int item : resultList) {
      if (item == 1) {
        count1++;
      } else if (item == 2) {
        count2++;
      } else if (item == 3) {
        count3++;
      } else if (item == 4) {
        count4++;
      } else if (item == 5) {
        count5++;
      }
    }

    log.add("   Bewertung -> " + resultList.size() + " Zwischenergebnisse!");
    log.add("             -> [1] x " + count1);
    log.add("             -> [2] x " + count2);
    log.add("             -> [3] x " + count3);
    log.add("             -> [4] x " + count4);
    log.add("             -> [5] x " + count5);

    // Gesamtbewertung
    if (count1 >= 2 && count3 <= 1 && count4 == 0 && count5 == 0) {
      result = 1;
    } else if ((count1 + count2) >= 2 && count3 <= 1 && count4 == 0 && count5 == 0) {
      result = 2;
    } else if ((count1 + count2 + count3) >= 2 && count4 <= 1 && count5 == 0) {
      result = 3;
    } else if ((count1 + count2 + count3 + count4) >= 2) {
      result = 4;
    } else {
      result = 5;
    }
    log.add("   Vorläufige Gesamtbewertung -> " + result);

    //long hangneigung = ((KeyAttribute) this.profil.getAttribute("hangneigung")).getId(); schon vorher deklariert
    if (hangneigung == 11 || hangneigung == 12 || hangneigung == 13
            || hangneigung == 21 || hangneigung == 22 || hangneigung == 23) {
      // keine Korrektur
    } else if (hangneigung == 14 || hangneigung == 24) {
      result = result + 1;
      log.add("   Korrektur nach Hangneigung -> +1");
    } else if (hangneigung == 15 || hangneigung == 25) {
      result = result + 2;
      log.add("   Korrektur nach Hangneigung -> +2");
    } else if (hangneigung == 16 || hangneigung == 17 || hangneigung == 26 || hangneigung == 27) {
      result = 5;
      log.add("   Korrektur nach Hangneigung -> 5");
    }

    if (result > 5) {
      result = 5;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Leben_Kult", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1C1min() {
    //  output
    log.add("-------------------");
    log.add("5) " + this.profil.getAttributeLabel("Retent_min"));
    Integer result = null;

    int bb = this.getBB();

    log.add("   BB -> 0 bis " + bb + " cm");
    log.add("   Für die WSV-Summe und den minimalen kf-Wert werden NUR Horizonte berücksichtigt, die sich zur Gänze in diesem Bereich befinden!");

    float wsvSum = -9999;
    float kfmin = -9999;
    // wenn BB den ganzen Profil abdeckt, einfach Sx08 und Sx10 verwenden
    if (bb == 100) {
      log.add("   BB deckt das gesamte Profil ab, einfach die entsprechenden Parameter verwenden");
      wsvSum = this.profil.getAttributeValueFloat("Sx08_WSVges");
      if (wsvSum < 0) {
        log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx08_WSVges"));
        return;
      }
      kfmin = this.profil.getAttributeValueFloat("Sx10_kfmin");
      if (kfmin < 0) {
        log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx10_kfmin"));
        return;
      }

    } else {
      log.add("   Suche der Horizonte, die über die BB-Untergenze liegen");
      ArrayList<CmsHorizont> aboveBB = new ArrayList<CmsHorizont>();
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= bb) {
          aboveBB.add(h);
        }
      }

      if (aboveBB.isEmpty()) {
        log.add("   FEHLER => Keine Horizont im BB! Fällt vielleicht der GW-Flurabstand innerhalb des 1. Horizontes?");
        return;
      }

      // Summe des WSV
      float sumPx10 = 0;
      // Minimaler kf-Wert für BB
      float minKF = Float.MAX_VALUE;
      for (CmsHorizont h : aboveBB) {
        // wenn px10 für diese Horizont vorhanden, aufsummieren
        float px10 = h.getAttributeValueFloat("Px10_WSV");
        if (px10 < 0) {
          log.add("   FEHLER => WSW nicht für alle Horizonten vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        sumPx10 += px10;

        // wenn px12 kleiner als Minimumwert
        float px12 = h.getAttributeValueFloat("Px12_kf");
        if (px12 < 0) {
          log.add("   FEHLER => kf-Wert nicht für alle Horizonte vorhanden. Siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        if (px12 < minKF) {
          minKF = px12;
        }
      }
      // Parameter für die Berechnung setzen
      if (minKF >= 0 && minKF != Float.MAX_VALUE) {
        kfmin = minKF;
      }
      wsvSum = sumPx10;
    }

    // Bewertung nach Tabelle
    if (wsvSum < 0) {
      log.add("   FEHLER => Summe des Wasserspeichervermögens für den BB konnte nicht berechnet werden.");
      return;
    } else if (kfmin < 0) {
      log.add("   FEHLER => Minimaler kf-Wert im BB konnte nicht ermittelt werden.");
      return;
    }

    log.add("   Bewertung -> min. kf-Wert: " + kfmin + " cm/d    WSV-Summe: " + wsvSum + " l/m²");
    if (kfmin <= 7) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 5;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 5;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 5;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 4;
      } else if (wsvSum > 300) {
        result = 4;
      }
    } else if (kfmin > 7 && kfmin <= 15) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 5;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 4;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 4;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 3;
      } else if (wsvSum > 300) {
        result = 3;
      }
    } else if (kfmin > 15 && kfmin <= 30) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 4;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 4;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 3;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 2;
      } else if (wsvSum > 300) {
        result = 2;
      }
    } else if (kfmin > 30 && kfmin <= 40) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 4;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 3;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 2;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 2;
      } else if (wsvSum > 300) {
        result = 2;
      }
    } else if (kfmin > 40 && kfmin <= 100) {
      if (wsvSum <= 60) {
        result = 3;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 3;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 2;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 2;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 2;
      } else if (wsvSum > 300) {
        result = 1;
      }
    } else if (kfmin > 100) {
      if (wsvSum <= 60) {
        result = 1;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 1;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 1;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 1;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 1;
      } else if (wsvSum > 300) {
        result = 1;
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Retent_min", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);

  }

  public void calculate1C1ave() {
    log.add("-------------------");
    log.add("6) " + this.profil.getAttributeLabel("Retent_ave"));
    Integer result = null;

    int bb = this.getBB();
    log.add("   BB -> 0 bis " + bb + " cm");
    log.add("   Für die WSV-Summe und den minimalen kf-Wert werden NUR Horizonte berücksichtigt, die sich zur Gänze in diesem Bereich befinden!");

    float wsvSum = -9999;
    float kfave = -9999;
    // wenn BB den ganzen Profil abdeckt, einfach Sx08 und Sx11 verwenden
    if (bb == 100) {
      log.add("   BB deckt den gesamten Profil ab, einfach die entsprechenden Parameter verwenden");
      wsvSum = this.profil.getAttributeValueFloat("Sx08_WSVges");
      if (wsvSum < 0) {
        log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx08_WSVges"));
        return;
      }
      kfave = this.profil.getAttributeValueFloat("Sx11_kfave");
      if (kfave < 0) {
        log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx11_kfave"));
        return;
      }
    } else {
      log.add("   Suche der Horizonte, die über die BB-Untergenze liegen");
      ArrayList<CmsHorizont> aboveBB = new ArrayList<CmsHorizont>();

      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= bb) {
          aboveBB.add(h);
        }
      }

      if (aboveBB.isEmpty()) {
        log.add("   FEHLER => Keine Horizont im BB! Fällt z.B. vielleicht der GW-Flurabstand innerhalb des 1. Horizontes?");
        return;
      }

      // Durchschnittlicher kf-Wert für BB
      float sumKF = 0;
      float sumPx10 = 0;
      int sumThickness = 0;
      for (CmsHorizont h : aboveBB) {
        int thickness = h.getThickness();
        float px12 = h.getAttributeValueFloat("Px12_kf");
        if (px12 < 0) {
          log.add("   FEHLER => kf-Wert nicht für alle Horizonte vorhanden. Siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        sumKF += (thickness / px12);
        sumThickness += thickness;

        // wenn px10 für diese Horizont vorhanden, aufsummieren
        float px10 = h.getAttributeValueFloat("Px10_WSV");
        if (px10 < 0) {
          log.add("   FEHLER => WSW nicht für alle Horizonten vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        sumPx10 += px10;

      }
      if (sumKF > 0) {
        kfave = sumThickness / sumKF; // siehe Berechnung Sx11, hier nur für den BB
        log.add("   Berechnung des durchschnittlichen kf-Wert");
        log.add("   Summe Mächtigkeit aller berücksichtigen Horizonte");
        log.add("   Entspricht nicht unbedingt BB, da dieser auch über GW-Flurabstand definiert werden kann!");
        log.add("   Mächtigkeit -> " + sumThickness);
        log.add("   Durchschn. kf-Wert -> " + kfave);
      }
      wsvSum = sumPx10;
      log.add("   WSV-Summe -> " + wsvSum);
    }

    // Berechnung nach Tabelle
    if (wsvSum < 0) {
      log.add("   FEHLER => Summe des Wasserspeichervermögens für den BB konnte nicht berechnet werden.");
      return;
    } else if (kfave < 0) {
      log.add("   FEHLER => Durchschn. kf-Wert konnte nicht berechnet werden.");
      return;
    }

    log.add("   Bewertung -> durchschn. kf-Wert: " + kfave + " cm/d     WSV-Summe: " + wsvSum + " l/m²");
    if (kfave <= 7) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 5;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 5;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 5;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 4;
      } else if (wsvSum > 300) {
        result = 4;
      }
    } else if (kfave > 7 && kfave <= 15) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 5;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 4;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 4;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 3;
      } else if (wsvSum > 300) {
        result = 3;
      }
    } else if (kfave > 15 && kfave <= 30) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 50 && wsvSum <= 90) {
        result = 4;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 4;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 3;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 2;
      } else if (wsvSum > 300) {
        result = 2;
      }
    } else if (kfave > 30 && kfave <= 40) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 4;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 3;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 2;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 2;
      } else if (wsvSum > 300) {
        result = 2;
      }
    } else if (kfave > 40 && kfave <= 100) {
      if (wsvSum <= 60) {
        result = 3;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 3;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 2;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 2;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 2;
      } else if (wsvSum > 300) {
        result = 1;
      }
    } else if (kfave > 100) {
      if (wsvSum <= 60) {
        result = 1;
      } else if (wsvSum > 60 && wsvSum <= 90) {
        result = 1;
      } else if (wsvSum > 90 && wsvSum <= 140) {
        result = 1;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 1;
      } else if (wsvSum > 220 && wsvSum <= 300) {
        result = 1;
      } else if (wsvSum > 300) {
        result = 1;
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Retent_ave", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  /**
   * Kurzfristiges Rückhaltevermögen für Starkniederschläge.
   */
  public void calculate1C2() {
    log.add("-------------------");
    log.add("7) " + this.profil.getAttributeLabel("Retent_stark"));
    Integer result = null;

    float sx07 = this.profil.getAttributeValueFloat("Sx07_LKoben");
    if (sx07 < 0) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx07_LKoben"));
      return;
    }

    float precip = project.getAttributeValueFloat("precip_crit");
    if (precip < 0) {
      log.add("   FEHLER => Es fehlt der Bemessungsniederschlag!");
      return;
    }

    // kfmin
    float kfmin = this.profil.getAttributeValueFloat("Sx10_kfmin");
    if (kfmin < 0) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx10_kfmin"));
      return;
    }

    boolean festgestein = false;
    boolean grundwasser = false;
    float gruendigkeit = this.profil.getAttributeValueFloat("gruendigkeit_wert");
    for (CmsHorizont h : this.horizonte) {
      if ((h.getAttributeValueFloat("Px02_Sk") == 100) &&  (gruendigkeit < 25.0f)) { // hier noch zusätzliche Bedingung eingebaut weil nur Festgestein eher sinnlos
        log.add("      Festgestein im Untergrund und Gründigkeit kleiner 25 cm!");
        festgestein = true;
      }
    }
    if (this.profil.getAttributeValueBoolean("grundwasser")) { //die bedingung flurabstand < 1m ist sowieso
      // Grundwassereinfluss
      log.add("   Grundwassereinfluss!");
      grundwasser = true;
    }

    // Korrektur
    if (!festgestein && !grundwasser) {
      log.add("   Korrektur des Bemessungsniederschlags!");
      precip = precip - (kfmin / 2.4f);
    } else {
      log.add("   Keine Korrektur des Bemessungsniederschlags");
    }

    // Verhältnis Bemessungsniederschlag mit Wasseraufnahmekapazität
    float rel = precip / sx07;

    if (rel <= 0.9f) {
      result = 1;
    } else if (rel > 0.9f && rel <= 1.2f) {
      result = 2;
    } else if (rel > 1.2f && rel <= 2.0f) {
      result = 3;
    } else if (rel > 2.0f && rel <= 3.0f) {
      result = 4;
    } else if (rel > 3.0f || this.profil.getAttributeValueFloat("flurabstand") < 1) {
      result = 5;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Retent_stark", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1C3() {
    log.add("-------------------");
    log.add("8) " + this.profil.getAttributeLabel("GWneu"));
    Integer result = null;

    int bb = this.getBB();
    log.add("   BB -> 0 bis " + bb + " cm");
    log.add("   Für die WSV-Summe und den minimalen kf-Wert werden NUR Horizonte berücksichtigt, die sich zur Gänze in diesem Bereich befinden!");

    float wsvSum = -9999;
    float kfmin = -9999;
    // wenn BB den ganzen Profil abdeckt, einfach Sx08 und Sx10 verwenden
    if (bb == 100) {
      log.add("   BB deckt das gesamte Profil ab, einfach die entsprechenden Parameter verwenden");
      wsvSum = this.profil.getAttributeValueFloat("Sx08_WSVges");
      if (wsvSum < 0) {
        log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx08_WSVges"));
        return;
      }
      kfmin = this.profil.getAttributeValueFloat("Sx10_kfmin");
      if (kfmin < 0) {
        log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx10_kfmin"));
        return;
      }

    } else {
      log.add("   Suche der Horizonte, die über die BB-Untergenze liegen");
      ArrayList<CmsHorizont> aboveBB = new ArrayList<CmsHorizont>();
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= bb) {
          aboveBB.add(h);
        }
      }

      if (aboveBB.isEmpty()) {
        log.add("   FEHLER => Keine Horizont im BB! Fällt vielleicht der GW-Flurabstand innerhalb des 1. Horizontes?");
        return;
      }

      // Summe des WSV
      float sumPx10 = 0;
      // Minimaler kf-Wert für BB
      float minKF = Float.MAX_VALUE;
      for (CmsHorizont h : aboveBB) {
        // wenn px10 für diese Horizont vorhanden, aufsummieren
        float px10 = h.getAttributeValueFloat("Px10_WSV");
        if (px10 < 0) {
          log.add("   FEHLER => WSW nicht für alle Horizonten vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        sumPx10 += px10;

        // wenn px12 kleiner als Minimumwert
        float px12 = h.getAttributeValueFloat("Px12_kf");
        if (px12 < 0) {
          log.add("   FEHLER => kf-Wert nicht für alle Horizonte vorhanden. Siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        if (px12 < minKF) {
          minKF = px12;
        }
      }
      // Parameter für die Berechnung setzen
      if (minKF >= 0 && minKF != Float.MAX_VALUE) {
        kfmin = minKF;
      }
      wsvSum = sumPx10;
    }

    // Bewertung nach Tabelle
    if (wsvSum < 0) {
      log.add("   FEHLER => Summe des Wasserspeichervermögens für den BB konnte nicht berechnet werden.");
      return;
    } else if (kfmin < 0) {
      log.add("   FEHLER => Minimaler kf-Wert im BB konnte nicht ermittelt werden.");
      return;
    }

    log.add("   Bewertung -> min. kf-Wert: " + kfmin + " cm/d    WSV-Summe: " + wsvSum + " l/m²");
    if (kfmin <= 7) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 140) {
        result = 4;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 3;
      } else if (wsvSum > 220) {
        result = 2;
      }
    } else if (kfmin > 7 && kfmin <= 15) {
      if (wsvSum <= 60) {
        result = 3;
      } else if (wsvSum > 60 && wsvSum <= 140) {
        result = 2;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 1;
      } else if (wsvSum > 220) {
        result = 1;
      }
    } else if (kfmin > 15 && kfmin <= 40) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 140) {
        result = 4;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 3;
      } else if (wsvSum > 220) {
        result = 2;
      }
    } else if (kfmin > 40) {
      if (wsvSum <= 60) {
        result = 5;
      } else if (wsvSum > 60 && wsvSum <= 140) {
        result = 5;
      } else if (wsvSum > 140 && wsvSum <= 220) {
        result = 5;
      } else if (wsvSum > 220) {
        result = 5;
      }
    }

    long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();

    // Korrektur
    if (bodentyp == 2100 || bodentyp == 2110 || bodentyp == 2111
            || bodentyp == 2112 || bodentyp == 2120) {
      log.add("   Moor!");
      log.add("   Korrektur -> 5");
      result = 5;
    }

    if (this.profil.getAttributeValueBoolean("grundwasser")) {
      // Grundwassereinfluss
      if (result != null && (result == 1 || result == 2)) {
        log.add("   Grundwassereinfluss!");
        log.add("   Korrektur -> 4");
        result = 4;
      }
      if (result != null && (result == 3 || result == 4 || result == 5)) {
        log.add("   Grundwassereinfluss!");
        log.add("   Korrektur -> 5");
        result = 5;
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("GWneu", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1C4() {
    log.add("-------------------");
    log.add("9) " + this.profil.getAttributeLabel("Naehrstoff"));
    Integer result = null;

    // Mb-Vorrat im effektiven Wurzelraum
    float sx15 = this.profil.getAttributeValueFloat("Sx15_MbWe");
    if (sx15 < 0) {
      log.add("   FEHLER => Komplexer Paramater fehlt! -> " + this.profil.getAttributeLabel("Sx15_MbWe"));
      return;
    }
    if (sx15 >= 0 && sx15 < 3000) {
      result = 5;
    } else if (sx15 >= 0 && sx15 >= 3000 && sx15 <= 6000) {
      result = 3;
    } else if (sx15 > 6000) {
      result = 1;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Naehrstoff", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);

  }

  public void calculate1C5() {
    log.add("-------------------");
    log.add("10) " + this.profil.getAttributeLabel("CO2_Senke"));
    // check Sx03_HMges
    if (!isAttributeValueSet("Sx03_HMges")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx03_HMges"));
      return;
    }

    Integer result = null;

    float sx03 = this.profil.getAttributeValueFloat("Sx03_HMges");

    if (sx03 >= 0 && sx03 >= 100) {
      result = 1;
    } else if (sx03 >= 0 && sx03 >= 50 && sx03 < 100) {
      result = 2;
    } else if (sx03 >= 0 && sx03 >= 20 && sx03 < 50) {
      result = 3;
    } else if (sx03 >= 0 && sx03 >= 10 && sx03 < 20) {
      result = 4;
    } else if (sx03 < 10) {
      result = 5;
    }

    // Korrektur nach Landnutzung
    Long[] landnutzung = this.profil.getAttributeMultikeyValueIds("landnutzung");
    List<Long> list = null;
    if (landnutzung != null) {
      list = new ArrayList<Long>(Arrays.asList(landnutzung));
    }
    if (list.contains(100L) || list.contains(110L) || list.contains(120L) || list.contains(130L) || list.contains(950L)) {
      log.add("   Landnutzung Wald, Laubwald, Mischwald, Nadelwald oder Moor!");
      log.add("   Korrektur -> 1");
      result = 1;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("CO2_Senke", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1D1() {
    log.add("-------------------");
    log.add("11) " + this.profil.getAttributeLabel("FiltPuff"));
    Integer result = null;

    long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();

    if (bodentyp >= 2100 && bodentyp <= 2120) {
      log.add("   Moorboden! Boden erhält die schlechteste Note");
      result = 5;
    } else {
      // Flurabstand in Meter
      Float flurabstand = this.profil.getAttributeValueFloat("flurabstand");
      // max depth in CM
      Integer maxDepth = null;
      if (flurabstand >= 0 && flurabstand <= 100) { //macht diese 100 m Abfrage sinn oder ist das noch von früher wegen cm? aber prinzipiell stört es nicht
        log.add("   GW-Flurabstand bekannt, es werden NUR Horizonte berücksichtigt, die sich vollständig DARÜBER befinden (Untergrenze <= GW-Flurabstand)");
        // maxdepth in CM rechnen
        maxDepth = Math.round(flurabstand) * 100;
      } else {
        for (CmsHorizont h : this.horizonte) {
          String bezeichnung = h.getAttributeValue("bezeichnung");
          if ("Gr".equalsIgnoreCase(bezeichnung) || "G2".equalsIgnoreCase(bezeichnung)) {
            int limit = h.getUpperLimit();
            if (limit >= 0) {
              log.add("   " + bezeichnung + "-Horizont gefunden! Es werden nur die sich darüber befindenden Horizonte berücksichtigt (" + limit + " cm)");
              maxDepth = limit;
            }
          }
        }
      }
      ArrayList<CmsHorizont> included = new ArrayList<CmsHorizont>();
      if (maxDepth != null) {
        for (CmsHorizont h : this.horizonte) {
          if (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= maxDepth) {
            included.add(h);
          }
        }
      } else {
        included = this.horizonte;
      }

      if (included.isEmpty()) {
        log.add("   FEHLER => Kein Horizont zu bewerten! Fällt vielleicht der GW-Flurabstand innerhalb des 1. Horizontes?");
        return;
      }

      log.add("   Für folgende Horizonte wird die relative Bindungsstärke für Cadmium bestimmt");
      StringBuilder sb = new StringBuilder();
      sb.append("   -> ");
      for (CmsHorizont h : included) {
        sb.append(h.getAttributeValue("bezeichnung")).append(" ");
      }
      log.add(sb.toString());

      Float sum = 0f;

      int sumThickness = 0;
      for (CmsHorizont h : included) {
        sumThickness += h.getThickness();
      }
      log.add("   Für die Gewichtung der Horizonte wird die Mächtigkeit aller berücksichtigten Horizonte aufsummiert");
      log.add("   -> " + sumThickness + " cm");

      for (CmsHorizont h : included) {

        Float cdrel = null;
        float ph = h.getAttributeValueFloat("ph_wert");
        String bezeichnung = h.getAttributeValue("bezeichnung");

        log.add("   '" + bezeichnung + "'-Horizont :");
        if (ph <= 0) {
          if (bezeichnung.startsWith("Cv")) {
            log.add("   Cv-Horizont ohne pH-Wert");
            float basenreichtum = h.getAttributeValueFloat("basenreichtum");
            if (basenreichtum < 0) {
              log.add("   FEHLER => Für ein Cv-Horizont ohne pH-Wert muss mindestens der Basenreichtum vorhanden sein");
              return;
            } else if (basenreichtum == 1) {
              cdrel = 5.0f;
            } else if (basenreichtum == 2) {
              cdrel = 1.0f;
            } else if (basenreichtum == 3) {
              cdrel = 3.0f;
            }
          } else {
            log.add("   FEHLER => pH-Wert nicht für alle Horizonte vorhanden");
            return;
          }
        } else if (ph < 2.8f) {
          cdrel = 0f;
        } else if (ph >= 2.8f && ph < 3.3f) {
          cdrel = 0.5f;
        } else if (ph >= 3.3f && ph < 3.8f) {
          cdrel = 1.0f;
        } else if (ph >= 3.8f && ph < 4.3f) {
          cdrel = 1.5f;
        } else if (ph >= 4.3f && ph < 4.8f) {
          cdrel = 2.0f;
        } else if (ph >= 4.8f && ph < 5.3f) {
          cdrel = 2.5f;
        } else if (ph >= 5.3f && ph < 5.8f) {
          cdrel = 3.5f;
        } else if (ph >= 5.8f && ph < 6.3f) {
          cdrel = 4.0f;
        } else if (ph >= 6.3f && ph < 6.7f) {
          cdrel = 4.5f;
        } else if (ph >= 6.7f ) { //sonst wird bei größer pH 8 kein Wert vergeben
          cdrel = 5.0f;
        }

        if (cdrel == null) {
          log.add("   FEHLER => Für den '" + h.getAttributeValue("bezeichnung") + "'-Horizont konnte die Bindungsstärke nicht bestimmt werden");
          return;
        }

        log.add("   Korrektur der rel. Bindungsstärke anhand Humusmenge");
        float px01 = h.getAttributeValueFloat("Px01_Hu");
        if (px01 < 0) {
          log.add("   ACHTUNG => Komplexer Parameter fehlt! -> '" + h.getAttributeValue("bezeichnung") + "': " + h.getAttributeLabel("Px01_Hu"));
          log.add("   HINWEIS => Bewertung wird genauer, wenn dieser Parameter vorhanden ist");
        } else if (px01 < 2) {
          log.add("   Keine Korrektur");
        } else if (px01 >= 2 && px01 < 8) {
          cdrel = cdrel + 0.5f;
          log.add("   Korrektur -> +0.5");
        } else if (px01 >= 8 && px01 < 15) {
          cdrel = cdrel + 1.0f;
          log.add("   Korrektur -> +1.0");
        } else if (px01 >= 15) {
          cdrel = cdrel + 1.5f;
          log.add("   Korrektur -> +1.5");
        }
// Hier habe ich eine hasAttribute abfrage eingeführt da vorher der tongehalt immer gleich 0 gesetzt wurde (wahrscheinlich weil float nicht null sein darf?!)
// jedenfalls soll die bodenart ansonst verwendet werden. Ich habe auch die variablge tongehalt durch bodenart ersetzt da diese hier falsch am platz war
        log.add("   Korrektur der rel. Bindungsstärke anhand Tongehalt bzw. Bodenart");
         if (h.hasAttributeValue("ton")) {             
            float tongehalt = h.getAttributeValueFloat("ton");
            log.add("   " + h.getAttributeLabel("ton") + " gefunden");
            log.add("   -> " + h.getAttributeValueFloat("ton") + " %");
            log.add("   Laborwert wird übernommen.");
            if (tongehalt >= 0) {
                log.add("   Tongehalt gefunden -> Korrektur");
                if (tongehalt < 12) {
                log.add("   Keine Korrektur");
                } else if (tongehalt >= 12) {
                cdrel = cdrel + 0.5f;
                log.add("   Korrektur -> + 0.5");
                }
                }
         } else {
       // hier kommt dann die bodenart ins 
        long bodenart = defineBodenart(h);
        if (bodenart >= 0) {
          log.add("   Bodenart gefunden -> Korrektur");
          if (bodenart <= 231) {
            log.add("   Keine Korrektur");
          } else if (bodenart >= 313) { //hier tongehalt durch bodenart ersetzt
            cdrel = cdrel + 0.5f;
            log.add("   Korrektur -> +0.5");
          } else {
            log.add("   Keine Korrektur");
          }
        } else {
          log.add("   ACHTUNG => Es fehlt sowohl Tongehalt als auch Bodenart! -> siehe '" + h.getAttributeValue("bezeichnung") + "'-Horizont");
          log.add("   HINWEIS => Bewertung wird genauer, wenn zumindest einer dieser Parameter vorhanden ist");
        }
         }

        log.add("   Korrektur der rel. Bindungsstärke anhand Skelettgehalt");
        float px02 = h.getAttributeValueFloat("Px02_Sk");
        if (px02 >= 0) {
          cdrel = cdrel * (100 - px02) / 100;
          log.add("   Korrektur erfolgreich!");
        } else {
          log.add("   ACHTUNG => Es fehlt der Skelettgehalt! -> siehe '" + h.getAttributeValue("bezeichnung") + "'-Horizont");
          log.add("   HINWEIS => Bewertung wird genauer, wenn dieser Parameter vorhanden ist");
        }

        sum += (cdrel * h.getThickness() / 100.0f);
      }
      log.add("   Summe (Cd-rel) für das gesamte Profil -> " + sum);
      if (sum < 1.5) {
        result = 5;
      } else if (sum >= 1.5 && sum < 2.5) {
        result = 4;
      } else if (sum >= 2.5 && sum < 3.5) {
        result = 3;
      } else if (sum >= 3.5 && sum < 4.5) {
        result = 2;
      } else if (sum >= 4.5) {
        result = 1;
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("FiltPuff", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1D2() {
    log.add("-------------------");
    log.add("12) " + this.profil.getAttributeLabel("Transform"));
    Integer result = null;

    // Liste der Horizonte die in die Bewertung fließen
    ArrayList<CmsHorizont> included = new ArrayList<CmsHorizont>();

    log.add("   Suche der Horizonte, die für die Bewertung relevant sind");
    Integer maxDepth = 100; //WARUM 100 cm? aber wohl prinzipiell kein problem
    for (CmsHorizont h : this.horizonte) {
      String bezeichnung = h.getAttributeValue("bezeichnung");
      if ("Gr".equalsIgnoreCase(bezeichnung) || "G2".equalsIgnoreCase(bezeichnung) || bezeichnung.startsWith("S")) {
        int limit = h.getUpperLimit();
        if (limit >= 0) {
          log.add("   " + bezeichnung + "-Horizont gefunden! Es werden nur Horizonte darüber berücksichtigt (Obergrenze -> " + limit + " cm)");
          maxDepth = limit;
        }
      }
    }

    for (CmsHorizont h : this.horizonte) {
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int tiefe = h.getAttributeValueInt("tiefe");
      if ((bezeichnung.contains("O") || bezeichnung.contains("H") || bezeichnung.contains("A"))
              && tiefe <= maxDepth) {
        included.add(h);
      } else if (h.getAttributeValueFloat("Px01_Hu") > 2 && tiefe <= maxDepth) {
        included.add(h);
      } else if (h.getAttributeValueFloat("Px01_Hu") < 0) {
        log.add("   ACHTUNG => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px01_Hu"));
        log.add("   HINWEIS => Bewertung wird genauer wenn dieser Prameter vorhanden ist!");
      }
    }

    if (included.isEmpty()) {
      log.add("   FEHLER => Keine Horizonte zu bewerten!");
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("   Berücksichtigte Horizonte -> ");
    for (CmsHorizont h : included) {
      sb.append(h.getAttributeValue("bezeichnung")).append(" ");
    }
    log.add(sb.toString());

    // Humusmenge im Oberboden
    float hmGesamt = 0;
    for (CmsHorizont h : included) {
      float px06 = h.getAttributeValueFloat("Px06_HM");
      if (px06 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + h.getAttributeValue("bezeichnung") + "': " + h.getAttributeLabel("Px01_Hu"));
        return;
      }
      hmGesamt += px06;
    }
    log.add("   Zwischensumme Humusmenge -> " + hmGesamt);

    // Tonmenge im Oberboden
    float tmGesamt = 0;
    for (CmsHorizont h : included) {
      float px05 = h.getAttributeValueFloat("Px05_TM");
      if (px05 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + h.getAttributeValue("bezeichnung") + "': " + h.getAttributeLabel("Px05_TM"));
        return;
      }
      tmGesamt += px05;
    }
    log.add("   Zwischensumme Tonmenge -> " + tmGesamt);

    // pH-Wert
    float ph = -1;
    if (included.size() == 1) {
      log.add("   Nur ein Horizont, pH-Wert wird direkt verwendet!");
      ph = included.get(0).getAttributeValueFloat("ph_wert");
      if (ph < 0) {
        log.add("   FEHLER => pH-Wert fehlt! -> siehe '" + included.get(0).getAttributeValue("bezeichnung") + "'");
        return;
      }
    } else {
      float px04Sum = 0;
      for (CmsHorizont h : included) {
        float sx01 = this.profil.getAttributeValueFloat("Sx01_FBges");
        if (sx01 < 0) {
          log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx01_FBges"));
          return;
        }
        float px04 = h.getAttributeValueFloat("Px04_FB");
        if (px04 < 0) {
          log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + h.getAttributeValue("bezeichnung") + "': " + h.getAttributeLabel("Px04_FB"));
          return;
        }
        float ph_wert = h.getAttributeValueFloat("ph_wert");
        if (ph_wert < 0) {
          log.add("   FEHLER => pH-Wert fehlt! -> siehe '" + included.get(0).getAttributeValue("bezeichnung") + "'");
          return;
        }
        ph += (px04 * ph_wert);
        px04Sum += px04;
      }
      // mittelwert
      ph = ph / px04Sum;
    }

    if (ph < 0) {
      log.add("   FEHLER => Mittlerer pH-Wert im Oberboden konnte nicht berechnet werden!");
      return;
    }

    // Mikrobielle Abbauleistung (1=niedrig, 2=mittel, 3=hoch)
    int abbau = 0;
    String humusform = this.profil.getAttributeValue("humusform");
    if (humusform.equalsIgnoreCase("Rohhumus")
            || (humusform.equalsIgnoreCase("Feuchtmoder") && ph < 5)
            || (humusform.equalsIgnoreCase("Feuchtrohhumus"))
            || (humusform.equalsIgnoreCase("Hochmoortorf"))
            || (humusform.equalsIgnoreCase("Anmoorhumus") && ph < 5)) {
      abbau = 1;
    } else if (humusform.equalsIgnoreCase("Mull")
            || (humusform.equalsIgnoreCase("Moderartiger Mull"))
            || (humusform.equalsIgnoreCase("Anmoormull") && ph < 5)
            || (humusform.equalsIgnoreCase("Moder"))
            || (humusform.equalsIgnoreCase("Feuchtmoder") && ph >= 5)
            || (humusform.equalsIgnoreCase("Anmoorhumus") && ph >= 5)
            || (humusform.equalsIgnoreCase("Niedermoortorf") && ph < 5)) {
      abbau = 2;
    } else if ((humusform.equalsIgnoreCase("Anmoormull") && ph >= 5)
            || (humusform.equalsIgnoreCase("Feuchtmull"))
            || (humusform.equalsIgnoreCase("Niedermoortorf") && ph >= 5)) {
      abbau = 3;
    }

    if (abbau == 0) {
      log.add("   FEHLER => Mikrobielles Abbauvermögen konnte nicht berechnet werden!");
      return;
    }

    log.add("   Mikrobielles Abbauvermögen (Klasse) -> " + abbau);

    // Bewertung nach Matrix
    if (hmGesamt < 13) {
      if (tmGesamt < 100) {
        switch (abbau) {
          case 1:
            result = 5;
            break;
          case 2:
            result = 5;
            break;
          case 3:
            result = 5;
            break;
        }
      } else if (tmGesamt >= 100 && tmGesamt <= 300) {
        switch (abbau) {
          case 1:
            result = 5;
            break;
          case 2:
            result = 4;
            break;
          case 3:
            result = 3;
            break;
        }
      } else if (tmGesamt > 300 && tmGesamt <= 450) {
        switch (abbau) {
          case 1:
            result = 5;
            break;
          case 2:
            result = 3;
            break;
          case 3:
            result = 3;
            break;
        }
      } else if (tmGesamt > 450) {
        switch (abbau) {
          case 1:
            result = 4;
            break;
          case 2:
            result = 3;
            break;
          case 3:
            result = 2;
            break;
        }
      }
    } else if (hmGesamt >= 13 && hmGesamt <= 25) {
      if (tmGesamt < 100) {
        switch (abbau) {
          case 1:
            result = 5;
            break;
          case 2:
            result = 5;
            break;
          case 3:
            result = 4;
            break;
        }
      } else if (tmGesamt >= 100 && tmGesamt <= 300) {
        switch (abbau) {
          case 1:
            result = 4;
            break;
          case 2:
            result = 3;
            break;
          case 3:
            result = 3;
            break;
        }
      } else if (tmGesamt > 300 && tmGesamt <= 450) {
        switch (abbau) {
          case 1:
            result = 3;
            break;
          case 2:
            result = 3;
            break;
          case 3:
            result = 2;
            break;
        }
      } else if (tmGesamt > 450) {
        switch (abbau) {
          case 1:
            result = 3;
            break;
          case 2:
            result = 2;
            break;
          case 3:
            result = 1;
            break;
        }
      }
    } else if (hmGesamt > 25 && hmGesamt <= 40) {
      if (tmGesamt < 100) {
        switch (abbau) {
          case 1:
            result = 5;
            break;
          case 2:
            result = 4;
            break;
          case 3:
            result = 3;
            break;
        }
      } else if (tmGesamt >= 100 && tmGesamt <= 300) {
        switch (abbau) {
          case 1:
            result = 3;
            break;
          case 2:
            result = 3;
            break;
          case 3:
            result = 2;
            break;
        }
      } else if (tmGesamt > 300 && tmGesamt <= 450) {
        switch (abbau) {
          case 1:
            result = 3;
            break;
          case 2:
            result = 2;
            break;
          case 3:
            result = 1;
            break;
        }
      } else if (tmGesamt > 450) {
        switch (abbau) {
          case 1:
            result = 2;
            break;
          case 2:
            result = 1;
            break;
          case 3:
            result = 1;
            break;
        }
      }
    } else if (hmGesamt > 40) {
      if (tmGesamt < 100) {
        switch (abbau) {
          case 1:
            result = 4;
            break;
          case 2:
            result = 3;
            break;
          case 3:
            result = 2;
            break;
        }
      } else if (tmGesamt >= 100 && tmGesamt <= 300) {
        switch (abbau) {
          case 1:
            result = 2;
            break;
          case 2:
            result = 2;
            break;
          case 3:
            result = 1;
            break;
        }
      } else if (tmGesamt > 300 && tmGesamt <= 450) {
        switch (abbau) {
          case 1:
            result = 2;
            break;
          case 2:
            result = 1;
            break;
          case 3:
            result = 1;
            break;
        }
      } else if (tmGesamt > 450) {
        switch (abbau) {
          case 1:
            result = 1;
            break;
          case 2:
            result = 1;
            break;
          case 3:
            result = 1;
            break;
        }
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Transform", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1D3() {
    log.add("-------------------");
    log.add("13) " + this.profil.getAttributeLabel("FiltPuff_Org"));
    Integer result = null;

    // Liste der Horizonte die in die Bewertung fließen
    ArrayList<CmsHorizont> included = new ArrayList<CmsHorizont>();

    log.add("   Für die Bewertung relevante Horizonte werden gesucht");
    Integer maxDepth = 100;
    for (CmsHorizont h : this.horizonte) {
      String bezeichnung = h.getAttributeValue("bezeichnung");
      if ("Gr".equalsIgnoreCase(bezeichnung) || "G2".equalsIgnoreCase(bezeichnung) || bezeichnung.startsWith("S")) {
        int limit = h.getUpperLimit();
        if (limit >= 0) {
          log.add("   " + bezeichnung + "-Horizont gefunden! Es werden nur Horizonte darüber berücksichtigt (Obergrenze -> " + limit + " cm)");
          maxDepth = limit;
        }
      }
    }

    for (CmsHorizont h : this.horizonte) {
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int tiefe = h.getAttributeValueInt("tiefe");
      if ((bezeichnung.contains("O") || bezeichnung.contains("T") || bezeichnung.contains("A"))
              && tiefe <= maxDepth) {
        included.add(h);
      } else if (h.getAttributeValueFloat("Px01_Hu") > 2 && tiefe <= maxDepth) {
        included.add(h);
      } else if (h.getAttributeValueFloat("Px01_Hu") < 0) {
        log.add("   ACHTUNG => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px01_Hu"));
        log.add("   HINWEIS => Bewertung wird genauer wenn dieser Prameter vorhanden ist!");
      }
    }

    if (included.isEmpty()) {
      log.add("   FEHLER => Keine Horizonte zu bewerten!");
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("   Berücksichtigte Horizonte -> ");
    for (CmsHorizont h : included) {
      sb.append(h.getAttributeValue("bezeichnung")).append(" ");
    }
    log.add(sb.toString());

    // Berechnung für die Horizonte die sich in der Liste befinden
    // Mächtigkeit
    float maechtigkeit = 0; //hab aus int float gemacht da sonst die 0.5 cm auflagen nicht gescheit zählt
    // Summe
    float orgRelSum = 0;
    for (CmsHorizont h : included) {
      float orgRelHumus = -1;
      String bezeichnung = h.getAttributeValue("bezeichnung");
      log.add("   '" + bezeichnung + "'-Horizont");
      // Hier Moore und Torfe eingebaut, allerdings passen die zersetzungklassen der Elemente nicht mit der Anleitung zusammen
      if(bezeichnung.contains("T")){
      log.add("'Torfhorizont gefunden! Org_rel_hum anhand von Torfzersetzungsstufe ");
      if(!h.hasAttributeValue("torf_zersetzung")){
          log.add(" Fehler! Torfzersetzungsgrad  fehlt ");
      } else{
      long zersetzung =((KeyAttribute) h.getAttribute("torf_zersetzung")).getId();
      float thickness = h.getThickness();
      if(zersetzung == 1 || zersetzung == 2){
          orgRelHumus = 2.0f;
      } else if(zersetzung == 3){
          orgRelHumus = 2.5f;
      } else if(zersetzung == 4 || zersetzung == 5){
          orgRelHumus = 3.0f;          
      }
      orgRelSum += orgRelHumus*thickness;
      maechtigkeit += thickness; 
      }
      } else{
      // Mittlere Bindungsstärke durch den Humusanteil
      
      // TODO : Überprüfen Moorböden und Torfe -  MIT FEB2017 versucht (fg)
      float px01 = h.getAttributeValueFloat("Px01_Hu");
      if (px01 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px01_Hu"));
      }
      if (px01 < 1) {
        orgRelHumus = 1.0f;
      } else if (px01 >= 1 && px01 < 2) {
        orgRelHumus = 1.5f;
      } else if (px01 >= 2 && px01 < 4) {
        orgRelHumus = 2.0f;
      } else if (px01 >= 4 && px01 < 8) {
        orgRelHumus = 2.0f;
      } else if (px01 >= 8 && px01 < 15) {
        orgRelHumus = 2.5f;
      } else if (px01 >= 15 && px01 < 30) {
        orgRelHumus = 3.0f;
      }
      log.add("   \"Mittlere\" relative Bindungsstärke durch Humusanteil -> " + orgRelHumus);

      // Mittlere Bindungsstärke durch den Tonanteil
      float orgRelTon = -1;
      float tongehalt = -1;
      if (h.hasAttributeValue("ton") && h.getAttributeValueFloat("ton") >= 0) { 
          log.add("   Laborwert für Tongehalt gefunden -> '" + h.getAttributeValueFloat("ton") + "%'");
          tongehalt = h.getAttributeValueFloat("ton");
        } 
      else {
        log.add("   Laborwert für Tongehalt nicht gefunden -> '" + bezeichnung + "'");
        log.add("   Versuch einer Ableitung durch Bodenart");
        long bodenart = this.defineBodenart(h);
        if (bodenart < 0) {
          log.add("   FEHLER => Angabe zur Bodenart fehlt! Es braucht zumindest die Bodenart, um die rel. Bindungsstärke des Tonanteils zu bestimmen -> '" + bezeichnung + "'");
          return;
        } else if (bodenart == 101 || bodenart == 121) {
          tongehalt = 2.5f;
        } else if (bodenart == 212) {
          tongehalt = 7.5f;
        } else if (bodenart == 202 || bodenart == 231) {
          tongehalt = 10f;
        } else if (bodenart == 332 || bodenart == 341 || bodenart == 313) {
          tongehalt = 20f;
        } else if (bodenart == 423 || bodenart == 403 || bodenart == 414) {
          tongehalt = 30f;
        } else if (bodenart == 534) {
          tongehalt = 45f;
        } else if (bodenart == 504) {
          tongehalt = 70f;
        }
      }

      if (tongehalt < 0) {
        log.add("   FEHLER => Tongehalt konnte nicht festgestellt werden! -> '" + bezeichnung + "'");
        return;
      }

      if (tongehalt >= 0 && tongehalt < 5) {
        orgRelTon = 1.0f;
      } else if (tongehalt >= 5 && tongehalt < 15) {
        orgRelTon = 1.5f;
      } else if (tongehalt >= 15 && tongehalt < 25) {
        orgRelTon = 2.0f;
      } else if (tongehalt >= 25 && tongehalt < 50) {
        orgRelTon = 2.5f;
      } else if (tongehalt >= 50) {
        orgRelTon = 3.0f;
      }

      log.add("   \"Mittlere\" relative Bindungsstärke durch Tonanteil -> " + orgRelTon);

      float orgRelTotal = orgRelHumus + orgRelTon;

      log.add("   Vorläufige \"Mittlere\" relative Bindungsstärke -> " + orgRelTotal);

      // Aufsummieren und Grobanteil abziehen
      float px02 = h.getAttributeValueFloat("Px02_Sk");
      if (px02 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px01_Hu"));
        return;
      }

      // 4) Modifikation der „mittleren“ Bindungsstärke für organische Schadstoffe nach Skelettgehalt
      float orgRelHrz = (orgRelTotal) * (100 - px02) / 100;
      log.add("   Abziehen des Grobanteils -> " + orgRelHrz);

      // in Relation zur Mächtigkeit
      float thickness = h.getThickness();
      //bis hierher das else oder?

      orgRelHrz = orgRelHrz * thickness;
      orgRelSum += orgRelHrz;
      maechtigkeit += thickness;
    }
    }

    log.add("   Überprüfen, ob Auflagehorizonte vorhanden sind");
    float olSize = this.profil.getAttributeValueFloat("ol_maecht");
    if (olSize > 0) {
      log.add("   Ol-Horizont gefunden!");
      log.add("   Rel. Bindungsstärke durch Humusanteil -> 2.0 ");
      log.add("   Mächtigkeit -> " + olSize + " cm");
      orgRelSum += 2.0*olSize;
      maechtigkeit += olSize; //das runden lass ich hier weg, weil es oft -horizonte mit 0.5cm gibt
    }
    float ofSize = this.profil.getAttributeValueFloat("of_maecht");
    if (ofSize > 0) {
      log.add("   Of-Horizont gefunden!");
      log.add("   Rel. Bindungsstärke durch Humusanteil -> 2.5 ");
      log.add("   Mächtigkeit -> " + ofSize + " cm"); //hier wurde olSize statt ofsize verwendet
      orgRelSum += 2.5*ofSize;
      maechtigkeit += ofSize; //hier auch
    }
    float ohSize = this.profil.getAttributeValueFloat("oh_maecht");
    if (ohSize > 0) {
      log.add("   Oh-Horizont gefunden!");
      log.add("   Rel. Bindungsstärke durch Humusanteil -> 3.0 ");
      log.add("   Mächtigkeit -> " + ohSize + " cm");
      orgRelSum += 3.0*ohSize;
      maechtigkeit += ohSize;
    }

    // 5) Bestimmung der „mittleren“ Bindungsstärke für organische Schadstoffe für das Gesamtprofil
    float orgRelProfil = orgRelSum / 100; //habe hier wieder die 100 cm nach Absprache mit Clemens genommen, damit man profile vergleichen kann! Die Anleitung ist leider nicht sehr klar bei diesem Punkt
    log.add("   Summe für das gesamte Profil -> " + orgRelProfil);

    // 6) Beurteilung der „mittleren“ Bindungsstärke für organische Schadstoffe für das Gesamtprofil
    if (orgRelProfil < 3) {
      result = 5;
    } else if (orgRelProfil >= 3 && orgRelProfil < 3.7) {
      result = 4;
    } else if (orgRelProfil >= 3.7 && orgRelProfil < 4.3) {
      result = 3;
    } else if (orgRelProfil >= 4.3 && orgRelProfil < 5.0) {
      result = 2;
    } else if (orgRelProfil >= 5.0) {
      result = 1;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("FiltPuff_Org", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1D4() {
    log.add("-------------------");
    log.add("14) " + this.profil.getAttributeLabel("FiltPuff_Nit"));
    Integer result = null;

    CmsHorizont oberbodenHrz = this.selectLargestOberbodenHrz();

    if (oberbodenHrz == null) {
      log.add("   FEHLER => Mächtigster Horizont des Oberbodens konnte nicht ermittelt werden. ");
      return;
    } else {
      // Bodenart (der genauere Wert wird verwendet)
      long bodenart = this.defineBodenart(oberbodenHrz);

      if (bodenart < 0) {
        log.add("   FEHLER => Angabe zur Bodenart fehlt! -> '" + oberbodenHrz.getAttributeValue("bezeichnung") + "'");
        return;
      }

      float anteilOFA = 0;
      if (bodenart == 414 || bodenart == 504 || bodenart == 534) {
        anteilOFA = 8f;
      } else if (bodenart == 202 || bodenart == 212 || bodenart == 332 || bodenart == 313
              || bodenart == 403 || bodenart == 423 ) {
        anteilOFA = 4.5f;
      } else if (bodenart == 101 || bodenart == 121 || bodenart == 231 || bodenart == 341) {
        anteilOFA = 1.5f;
      }

      float precip = project.getAttributeValueFloat("precip_year"); 
      float evaporation = project.getAttributeValueFloat("evaporation");

      if (precip < 0) {
        log.add("   FEHLER => Es fehlt der mittlerer Jahresniederschlag.");
        return;
      }
      if (evaporation < 0) {
        log.add("   FEHLER => Es fehlt die mittlere jährliche Verdunstung.");
        return;
      }

      float sickerwasser = (precip - evaporation) * (1 - anteilOFA / 100);

      float sx05 = this.profil.getAttributeValueFloat("Sx05_FKges");
      if (sx05 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx05_FKges"));
        return;
      }

      float swAustausch = sickerwasser / sx05;
      log.add("   Jährliche Austauschhäufigkeit -> " +swAustausch);

      if (swAustausch >= 2.5) {
        result = 5;
      } else if (swAustausch >= 1.5 && swAustausch < 2.5) {
        result = 4;
      } else if (swAustausch >= 1.0 && swAustausch < 1.5) {
        result = 3;
      } else if (swAustausch >= 0.7 && swAustausch < 1.0) {
        result = 2;
      } else if (swAustausch < 0.7) {
        result = 1;
      }
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("FiltPuff_Nit", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public void calculate1D5() {
    log.add("-------------------");
    log.add("15) " + this.profil.getAttributeLabel("Puff_sauer"));
    Integer result = null;

    log.add("   HINWEIS => CaCO3-Werte müssen nach Scheibler (gasvolumetrische Methode) bestimmt werden für genaue Berechnung, sonst kann nur ein Minimalwert für die carbonatabhängige Pufferkapazität berechnet werden!");

    //Pufferkapazität des gesamten Mineralbodens
    float minerPuff = 0;
    float humusPuff = 0;
    // Pufferkapazität für jeden Horizont berechnen
    for (CmsHorizont h : this.horizonte) {
      float carbonat = -1;
      String bezeichnung = h.getAttributeValue("bezeichnung");
      if (h.hasAttributeValue("carbonat_wert") && !(h.getAttributeValueFloat("carbonat_wert") < 0)) {
          log.add(" Carbonatwert aus Laboruntersuchung gefunden! -> '" + bezeichnung + "': " + h.getAttributeValueFloat("carbonat_wert") + "%");
          carbonat = h.getAttributeValueFloat("carbonat_wert");
      } else if (h.hasAttribute("carbonat_klasse")){
          log.add(" Carbonatklasse gefunden!   " + bezeichnung  + "    mit Karbonatklasse " + h.getKeyAttribute("carbonat_klasse").getId() );
        int carbonat_klasse_id = (int) h.getKeyAttribute("carbonat_klasse").getId();
        switch (carbonat_klasse_id) {
          case 12:
            carbonat = 0.0f;
            break;
          case 3:
            carbonat = 1.0f;
            break;
          case 4:
            carbonat = 3.0f;
            break;
          case 5:
            carbonat = 5.0f;
            break;
          }
      } else if (carbonat < 0) {
        log.add("   FEHLER => Carbonat (Wert oder Klasse) fehlt! -> '" + bezeichnung + "'");
        return;
      }
      float px04 = h.getAttributeValueFloat("Px04_FB");
      if (px04 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px04_FB"));
        return;
      }
      float px13 = h.getAttributeValueFloat("Px13_KAKpot");
      if (px13 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px13_KAKpot"));
        return;
      }
      float px15 = h.getAttributeValueFloat("Px15_Mb");
      if (px15 < 0) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> '" + bezeichnung + "': " + h.getAttributeLabel("Px15_Mb"));
        return;
      }

      // Berechnung der Pufferkapazität durch Carbonat für jeden (Mineralboden-)Horizont
      float carbpuff = px04 * (carbonat / 100) * 20;
      // Berechnung des Vorrates austauschbarer basischer Kationen an der KAKpot
      float mb = px04 * (px13 * px15 / 100);
      // Aufsummieren
      minerPuff += carbpuff * 100 + mb;
    }

    float maechtigkeit = 0;
    log.add("   Überprüfung, ob Auflagehorizonte vorhanden sind");
    log.add("   HINWEIS => Die Auflagehorizonte sind in Hinsicht auf der Bestimmung der Pufferkapazität der Humusauflage als ein Horizont zu betrachten.");
    log.add("              Dies ist notwendig, da die Humusform nicht differenziert aufgenommen wird.");

    float olSize = this.profil.getAttributeValueFloat("ol_maecht");
    if (olSize > 0) {
      log.add("   Ol-Horizont gefunden!");
      log.add("   Mächtigkeit -> " + olSize + " cm");
      maechtigkeit += olSize;
    }
    float ofSize = this.profil.getAttributeValueFloat("of_maecht");
    if (ofSize > 0) {
      log.add("   Of-Horizont gefunden!");
      log.add("   Mächtigkeit -> " + ofSize + " cm");
      maechtigkeit += ofSize; //hier war olsize statt ofsize
    }
    float ohSize = this.profil.getAttributeValueFloat("oh_maecht");
    if (ohSize > 0) {
      log.add("   Oh-Horizont gefunden!");
      log.add("   Mächtigkeit -> " + ohSize + " cm"); 
      maechtigkeit += ohSize; //hier war olsize statt ohsize
    }
    if (maechtigkeit > 0) {
      log.add("   Mächtigkeit der organischen Auflage -> " + maechtigkeit + " cm");
      long humusform = ((KeyAttribute) this.profil.getAttribute("humusform")).getId();
      if (humusform < 0) {
        log.add("   ACHTUNG => Organische Auflage vorhanden aber keine Humusform gefunden!");
        log.add("   HINWEIS => Bewertung wird genauer wenn dieser Parameter vorhanden ist!");
      } else if (humusform == 110 || humusform == 112 || humusform == 114 || humusform == 210) {
        log.add("   Mull!");
        humusPuff += 61 * 0.07 * maechtigkeit * 10; //wir glauben dass sich die Bayern hier mir den Einheiten vertan haben
      } else if (humusform == 130 || humusform == 230) {
        log.add("   Rohhumus!");
        humusPuff += 32 * 0.20 * maechtigkeit * 10; //0.25 passt nicht zu Tabelle in Anleitung u BayGLA, daher auf 0.2 geändert
      } else if (humusform == 120 || humusform == 220) {
        log.add("   Moder!");
        humusPuff += 39 * 0.13 * maechtigkeit * 10;
      }
    }

    log.add("   Humusauflage -> " + humusPuff + " cmolc/m²");
    log.add("   Mineralboden -> " + minerPuff + " cmolc/m²");
    float summe = minerPuff + humusPuff;
    log.add("   Summe -> " + summe + " cmolc/m²");

    if (summe < 1000) {
      result = 5;
    } else if (summe >= 1000 && summe < 3000) {
      result = 4;
    } else if (summe >= 3000 && summe < 10000) {
      result = 3;
    } else if (summe >= 10000 && summe < 30000) {
      result = 2;
    } else if (summe >= 30000) {
      result = 1;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Puff_sauer", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  /**
   * Potenzial als Archiv der Naturgeschichte
   */
  public void calculate2A() {
    log.add("-------------------");
    log.add("16) " + this.profil.getAttributeLabel("Arc_Nat"));
    Integer result = null;

    boolean natur = this.profil.getAttributeValueBoolean("naturarchiv");
    if (natur) {
      result = 1;
    } else if (!natur) {
      result = 5;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Arc_Nat", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  /**
   * Potenzial als Archiv der Kulturgeschichte
   */
  public void calculate2B() {
    log.add("-------------------");
    log.add("17) " + this.profil.getAttributeLabel("Arc_Kult"));
    Integer result = null;

    boolean kultur = this.profil.getAttributeValueBoolean("kulturarchiv");
    if (kultur) {
      result = 1;
    } else if (!kultur) {
      result = 5;
    }

    if (result == null) {
      log.add("   #######################");
      log.add("   SCHWERWIEGENDER FEHLER!");
      log.add("   #######################");
      return;
    }
    // save to profil
    this.profil.setAttributeValue("Arc_Kult", String.valueOf(result));
    log.add("   ERGEBNIS => " + result);
  }

  public ArrayList<String> getLog() {
    return log.getLog();
  }

}
