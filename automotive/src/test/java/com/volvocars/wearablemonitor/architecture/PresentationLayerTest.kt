package com.volvocars.wearablemonitor.architecture

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.runner.RunWith

@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(packages = ["com.volvocars.wearable_monitor"])
internal class PresentationLayerTest {
    @ArchTest
    val `view model subclasses should have correct name` =
        classes().that().areAssignableTo(ViewModel::class.java)
            .should().haveSimpleNameEndingWith("ViewModel")

    @ArchTest
    val `classes named ViewModel should have correct super class` =
        classes().that().haveSimpleNameEndingWith("ViewModel")
            .should().beAssignableTo(ViewModel::class.java)

    @ArchTest
    val `fragment subclasses should have correct name` =
        classes().that().areAssignableTo(Fragment::class.java)
            .should().haveSimpleNameEndingWith("Fragment")

    @ArchTest
    val `classes named Fragment should have correct super class` =
        classes().that().haveSimpleNameEndingWith("Fragment")
            .should().beAssignableTo(Fragment::class.java)

    @ArchTest
    val `activity subclasses shold have correct name` =
        classes().that().areAssignableTo(FragmentActivity::class.java)
            .should().haveSimpleNameEndingWith("Activity")

    @ArchTest
    val `classes named Activity shold have correct super class` =
        classes().that().haveSimpleNameEndingWith("Activity")
            .should().beAssignableTo(FragmentActivity::class.java)
}