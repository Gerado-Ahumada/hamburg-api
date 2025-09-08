package com.hamburg.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "statuses")
public class Status extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EStatus name;
    
    public Status() {
    }
    
    public Status(EStatus name) {
        this.name = name;
    }
    

    
    public EStatus getName() {
        return name;
    }
    
    public void setName(EStatus name) {
        this.name = name;
    }
}