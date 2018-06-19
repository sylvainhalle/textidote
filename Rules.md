Rules checked by TeXtidote
==========================

Style
-----

- A section title should start with a capital letter. [sh:001]
- A section title should not end with a punctuation symbol. [sh:002]
- A section title should not be written in all caps. The LaTeX stylesheet
  takes care of rendering titles in caps if needed. [sh:003]
- A figure caption should end with a period. [sh:004]

Citations and references
------------------------

- There should be one space before a \cite or \ref command [sh:c:001], and
  no space after [sh:c:002].

Figures
-------
- Every figure should have a label, and every figure should be referenced at
  least once in the text. [sh:figref]
- Figures should not refer to hard-coded local paths. [sh:relpath]

Typesetting
-----------

- You should not break lines manually in a paragraph. Either start a new
  paragraph or stay in the current one. [sh:nobreak]

Structure
---------

- A section should not contain a single sub-section. More generally, a division
  of level n should not contain a single division of level n+1. [sh:nsubdiv]

Potentially suspicious
----------------------

- There should be at least N words between two section headings (currently
  N=50). [sh:seclen]