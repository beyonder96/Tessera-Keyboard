with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

target = """        suggestion1?.text = predictions.getOrNull(0) ?: ""
        suggestion2?.text = predictions.getOrNull(1) ?: ""
        suggestion3?.text = predictions.getOrNull(2) ?: ""
    }"""

replacement = """        val s1 = predictions.getOrNull(0) ?: ""
        val s2 = predictions.getOrNull(1) ?: ""
        val s3 = predictions.getOrNull(2) ?: ""
        
        suggestion1?.text = s1
        suggestion1?.visibility = if (s1.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
        
        suggestion2?.text = s2
        suggestion2?.visibility = if (s2.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
        
        suggestion3?.text = s3
        suggestion3?.visibility = if (s3.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
    }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
