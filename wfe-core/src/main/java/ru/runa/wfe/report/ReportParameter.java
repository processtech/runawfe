package ru.runa.wfe.report;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * DTO for parameters data storage. These parameters must be input by the user in order to build report.
 */

@Entity
@Table(name = "REPORT_PARAMETER", indexes = { @Index(name = "IX_PARAMETER_REPORT_ID", columnList = "REPORT_ID") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReportParameter {
    private Long id;

    /**
     * Parameter name that is shown to user.
     */
    private String name;

    /**
     * Parameter type.
     */
    private ReportParameterType type;

    /**
     * Report parameter name.
     */
    private String innerName;

    /**
     * True if parameter is required and false if it's optional.
     */
    private boolean required;

    private ReportDefinition definition;

    public ReportParameter() {
    }

    public ReportParameter(String name, ReportParameterType type, String innerName, boolean required) {
        this.name = name;
        this.type = type;
        this.innerName = innerName;
        this.required = required;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_REPORT_PARAMETER", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "NAME", length = 1024, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "TYPE", length = 1024, nullable = false)
    @Enumerated(EnumType.STRING)
    public ReportParameterType getType() {
        return type;
    }

    public void setType(ReportParameterType type) {
        this.type = type;
    }

    @Column(name = "INNER_NAME", length = 1024, nullable = false)
    public String getInnerName() {
        return innerName;
    }

    public void setInnerName(String innerName) {
        this.innerName = innerName;
    }

    @Column(name = "REQUIRED", nullable = false)
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID", nullable = false)
    public ReportDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ReportDefinition definition) {
        this.definition = definition;
    }
}
